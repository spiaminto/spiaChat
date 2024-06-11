package chat.twenty.service;

import chat.twenty.domain.ChatRoom;
import chat.twenty.domain.RoomMember;
import chat.twenty.domain.TwentyMemberInfo;
import chat.twenty.dto.TwentyMessageDto;
import chat.twenty.enums.ChatMessageType;
import chat.twenty.enums.ChatRoomType;
import chat.twenty.enums.TwentyGameSubject;
import chat.twenty.exception.BanMemberNotValidException;
import chat.twenty.exception.TwentyGameAliveNotValidException;
import chat.twenty.exception.TwentyGameOrderNotValidException;
import chat.twenty.service.gpt.CustomGptService;
import chat.twenty.service.gpt.TwentyGameAnswer;
import chat.twenty.service.lower.ChatRoomService;
import chat.twenty.service.lower.RoomMemberService;
import chat.twenty.service.lower.TwentyMemberInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 스무고개 기능 관련  상위 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TwentyGameService {

    private final RoomMemberService memberService;
    private final ChatRoomService roomService;
    private final CustomGptService gptService;
    private final TwentyMemberInfoService memberInfoService;

    public void readyGame(Long roomId, Long userId) {
        memberService.twentyReady(roomId, userId);
    }

    public void unreadyGame(Long roomId, Long userId) {
        memberService.twentyUnready(roomId, userId);
    }

    // TODO try - catch 로 proceedGame 처럼.
    public boolean validateGameStart(Long roomId) {
        long memberCount = memberService.countRoomMember(roomId);
        long readyMemberCount = memberService.countTwentyReadyMemberByRoomId(roomId);
        log.info("validateGameStart() memberCount = {} readyMemberCount = {}", memberCount, readyMemberCount);
        return memberCount == readyMemberCount;
    }

    /**
     * 시작 검증후, 게임 시작
     * TWENTY_GAME_START
     */
    public TwentyMessageDto confirmGameStart(TwentyMessageDto twentyMessageDto) {

        ChatRoom findRoom = roomService.findRoomWithMembers(twentyMessageDto.getRoomId());

        // 정답 생성
        if (findRoom.getSubject() != TwentyGameSubject.CUSTOM) {
            findRoom.setTwentyAnswer(TwentyGameAnswer.getRandomAnswer(findRoom.getSubject()));
        }

        // 방 멤버에 따른 MemberInfo 생성 후 order 와 함께 저장 (alive 기본값 true)
        List<RoomMember> memberList = findRoom.getMembers(); // 방멤버 (전원 ready 상태여야 시작가능)
        Collections.shuffle(memberList); // 순서 랜덤화
        for (int i = 0; i < memberList.size(); i++) {
            RoomMember member = memberList.get(i);
            memberInfoService.save(TwentyMemberInfo.createNewMemberInfo(member.getUserId(), member.getRoom().getId(), i));
        }

        // GPT 활성화 처리 및 uuid 획득
        String gptUuid = gptService.activateGpt(twentyMessageDto.getRoomId(), twentyMessageDto.getUserId());

        List<TwentyMemberInfo> memberInfoList = memberInfoService.findByRoomId(twentyMessageDto.getRoomId());
        twentyMessageDto.setMemberInfoList(memberInfoList); // MemberInfo 초기값 설정
        twentyMessageDto.setGptUuid(gptUuid);
        return twentyMessageDto;
    }

    /**
     * 스무고개 순서 배정용 배열 생성 및 반환
     * @param twentyReadyMemberCount 전체 ready 상태 memberCount
     * TWENTY_GAME_START
     */
    //LEGACY
    protected List<Integer> makeOrderArray(long twentyReadyMemberCount) {
        // IntStream 으로 0 ~ readyMemberCount 만큼 orderList 생성
        List<Integer> orderList = IntStream.range(0, (int)twentyReadyMemberCount).boxed().collect(Collectors.toList());
        // 섞음
        Collections.shuffle(orderList);
        return orderList;
    }

    public TwentyMessageDto proceedStart(Long roomId, Long userId) {
        resetOrder(roomId);

        TwentyMessageDto gptRespMessage = gptService.sendGptTwentyRequest(roomId, userId);
        gptRespMessage.setNextUserId(memberInfoService.findByRoomIdAndOrder(roomId, 0).getUserId());

        return gptRespMessage;
    }

    /**
     * 스무고개 순서 초기화
     */
    protected void resetOrder(long roomId) {
        roomService.resetTwentyOrder(roomId);
        log.info("roomService.resetTwentyOrder() order been reset");
    }

    public TwentyMessageDto proceedGame(Long roomId, Long userId, int currentOrder) {
        // 필요한 엔티티 조회
        List<TwentyMemberInfo> memberInfoList = memberInfoService.findByRoomId(roomId);
        TwentyMemberInfo currentMemberInfo = memberInfoList.stream().
                filter(memberInfo -> memberInfo.getUserId() == userId).findFirst().orElse(null);
        ChatRoom currentRoom = roomService.findById(roomId);

        // 검증
        validateOrder(roomId, userId, currentMemberInfo.getTwentyOrder(), currentRoom.getTwentyNext()); // 순서검증
        validateAlive(roomId, userId, currentMemberInfo.isAlive()); // 플레이어 alive 검증

        // 순서 진행
        TwentyMemberInfo nextMemberInfo = getNextOrderMemberInfo(memberInfoList, currentOrder);
        currentRoom.setTwentyNext(nextMemberInfo.getTwentyOrder()); // 다음 순서 저장

        // GPT 질의
        TwentyMessageDto gptRespMessage = gptService.sendGptTwentyRequest(roomId, userId);
        gptRespMessage.setNextUserId(nextMemberInfo.getUserId());

        // GPT 대답 후처리
        gptRespMessage.setContent(postProcessGptAnswer(gptRespMessage.getContent(), currentRoom.getTwentyAnswer()));

        log.info("proceedGame() userId = {}, nextOrder = {}", nextMemberInfo.getUserId(), nextMemberInfo.getTwentyOrder());
        return gptRespMessage;
    }

    /**
     * 순서를 검증
     * @param targetOrder 현재 멤버의 순서
     * @param correctOrder 올바른 순서
     * <br> roomId, userId 는 에러, 로그용
     * @return
     */
    protected boolean validateOrder(Long roomId, Long userId, int targetOrder, int correctOrder) {
        if (targetOrder == correctOrder) {
            log.info("validateOrder() correctOrder is valid, correctOrder = " + correctOrder + " roomId = " + roomId);
            return true;
        } else {
            throw new TwentyGameOrderNotValidException("TwentyGameService.validateOrder() correctOrder is not valid," +
                    " correctOrder = " + correctOrder + " roomId = " + roomId, roomId, userId, correctOrder);
        }
    }

    /**
     * 플레이어 alive 상태 검증
     * @param alive 현재 멤버의 생존여부
     * <br> roomId, userId 는 에러, 로그용
     */
    protected boolean validateAlive(Long roomId, Long userId, boolean alive) {
        if (alive) {
            log.info("validateAlive() userId = " + userId + "isAlive = " + alive);
            return true;
        } else {
            throw new TwentyGameAliveNotValidException("TwentyGameService.validateAlive() not valid userId = "
                    + userId + "isAlive = " + alive, userId, roomId);
        }
    }

    /**
     * 멤버인포 리스트를 받아 다음 순서의 멤버인포를 반환 (순환하는 순서)
     */
    protected TwentyMemberInfo getNextOrderMemberInfo(List<TwentyMemberInfo> memberInfoList, int currentOrder) {
        long playerCount = memberInfoList.size();
        long loopLimit = playerCount; // 플레이어 카운트 이상으로 반복문을 못돌리도록 할 값
        int nextOrder = currentOrder;
        TwentyMemberInfo nextMemberInfo; // nullable

        do {
            nextOrder = ++nextOrder >= playerCount ? 0 : nextOrder; // 다음사람 순서계산 (순환)
            int nextOrderTemp = nextOrder;
            nextMemberInfo = memberInfoList.stream().filter(memberInfo -> memberInfo.getTwentyOrder() == nextOrderTemp).findFirst().orElse(null);
            loopLimit--; // playerCount-- 를 통해, 남은 playerCount 가 0이면 종료한다(보험).

        } while (!nextMemberInfo.isAlive() && loopLimit > 0); // 다음사람이 죽었으면 다시 다음사람 조회

        return nextMemberInfo;
    }

    /**
     * 게임의 진행을 매끄럽게 하기위해 필요에 따라 gpt 의 응답을 가공
     * 정답 판별중 일때는 사용하지 않음.
     */
    protected String postProcessGptAnswer(String gptResponse, String twentyAnswer) {
        String result = gptResponse;
        if (gptResponse.length() > 200) {
            // 200자 이상이면 180자로 자르고, 마지막에 ...(너무 긴 대답) 을 붙인다.
            result = gptResponse.substring(0, 180) + "...(너무 긴 대답)";

        } else if (gptResponse.contains("축하")) {
            // 정답 판별중이 아닐때, 축하(합니다) 메시지 전부 수정
            result = "대답할 수 없습니다. 다음 질문을 진행해 주세요.";

        } else if (twentyAnswer != null && gptResponse.contains(twentyAnswer)) {
            // 정답이 포함되어있으면 정답을 'ㅇㅇ' 로 바꾸고 반환
            result = gptResponse.replace(twentyAnswer, "ㅇㅇ");
        }
        return result;
    }

    public TwentyMessageDto proceedAnswer(Long roomId, TwentyMessageDto twentyMessageDto) {
        // 게임 진행후 GPT 질의 까지는 동일
        TwentyMessageDto gptRespMessage = proceedGame(roomId, twentyMessageDto.getUserId(), twentyMessageDto.getOrder());
        // em 에서 room 조회
        ChatRoom room = roomService.findById(roomId);

        // 정답인지 검증
        String twentyAnswer = room.getTwentyAnswer();
        boolean isAnswer = twentyMessageDto.getContent().contains(twentyAnswer);

        log.info("proceedAnswer() inputContent = {} gptRespMessage.getContent() = {}, isAnswer = {}", twentyMessageDto.getContent() , gptRespMessage.getContent(), isAnswer);

        if (isAnswer) {
            // 정답 처리
            finishGame(roomId);
            gptRespMessage.setType(ChatMessageType.TWENTY_GAME_END);
            gptRespMessage.setTwentyWinner(twentyMessageDto.getUsername());
            gptRespMessage.setContent(twentyMessageDto.getUsername() + " 님 정답입니다. 스무고개를 종료합니다. 정답: " + twentyAnswer);
        } else {
            // 오답처리
            memberInfoService.updateMemberAlive(twentyMessageDto.getUserId(), false);
            gptRespMessage.setTwentyDeadUserId(twentyMessageDto.getUserId());
            gptRespMessage.setContent("오답 입니다. 다음 질문을 진행해주세요. / " + twentyMessageDto.getUsername() + "님 사망");

            if (memberInfoService.isRoomAllDead(roomId)) {
                // 모든 유저가 정답을 맞히지 못함.
                gptRespMessage = proceedAbort(roomId, "모든 유저가 정답을 맞히지 못했습니다. 게임을 종료합니다. @정답: " + twentyAnswer);
            }
        }

        return gptRespMessage;
    }

    /**
     * 승리 이외의 조건에서 게임을 종료할때 사용
     * @param message : 프론트로 전달할 채팅 메시지
     */
    public TwentyMessageDto proceedAbort(Long roomId, String message) {
        finishGame(roomId);
        return TwentyMessageDto.createAbortMessage(roomId, message);
    }

    protected void finishGame(long roomId) {
        memberService.twentyUnreadyAll(roomId); // 모든유저 unready

        RoomMember roomOwner = memberService.findRoomOwner(roomId);
        gptService.deActivateGpt(roomId, roomOwner.getUserId()); // gpt 비활성화
        resetOrder(roomId); // 순서 초기화

        memberInfoService.deleteAllByRoomId(roomId); // memberInfo 삭제
    }

    public TwentyMessageDto banMember(Long roomId, Long userId, Long banUserId) {
        ChatRoom findRoom = roomService.findRoomWithMembers(roomId);
        List<RoomMember> members = findRoom.getMembers();
        RoomMember roomOwner = null;
        RoomMember banMember = null;

        for (RoomMember member : members) {
             if (member.getUserId() == userId) roomOwner = member;
             else if (member.getUserId() == banUserId) banMember = member;
        }

        if (roomOwner == null
                || banMember == null
                || !roomOwner.isRoomOwner()
                || roomOwner.getUserId().equals(banMember.getUserId())) {
            throw new BanMemberNotValidException("강퇴 검증 오류: 강퇴할수 없습니다.", roomId, userId, banUserId, ChatRoomType.TWENTY_GAME);
        }

        memberService.leaveRoom(roomId, banUserId);
        return TwentyMessageDto.createBanMessage(roomId, banUserId, banMember.getUsername());
    }

}
