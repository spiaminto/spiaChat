package chat.twenty.event.eventlistener.socket;

import chat.twenty.domain.RoomMember;
import chat.twenty.domain.User;
import chat.twenty.dto.TwentyMessageDto;
import chat.twenty.event.TwentyConnectEvent;
import chat.twenty.event.TwentyDisconnectEvent;
import chat.twenty.event.TwentySubscribeEvent;
import chat.twenty.event.TwentyUnsubscribeEvent;
import chat.twenty.service.lower.ChatRoomService;
import chat.twenty.service.lower.RoomMemberService;
import chat.twenty.service.lower.TwentyMemberInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * subUrl /topic/twenty-game/{roomId} 으로 오는 stomp 메시지의 이벤트 처리
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TwentyWebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomMemberService memberService;
    private final ChatRoomService roomService;
    private final TwentyMemberInfoService memberInfoService;

    @EventListener
    public void webSocketConnectListener(TwentyConnectEvent event) {
        Long currentUserId = event.getUser().getId();
        Long currentRoomId = event.getRoomId();

        memberService.connectToRoom(currentRoomId, currentUserId);
    }

    @EventListener
    public void webSocketSubscribeListener(TwentySubscribeEvent event) throws InterruptedException {
        Thread.sleep(100); // 메시지 씹힘 방지를 위해 0.1초 대기
        TwentyMessageDto twentyMessageDto = TwentyMessageDto.createSubscribeMessage(event.getUser().getId(), event.getUser().getUsername());

        // ConnectEvent 에서 입장메시지를 보내면, Subscribe 전에 보내서 씹히는 경우가 있다.
        messagingTemplate.convertAndSend("/topic/twenty-game/" + event.getRoomId(), twentyMessageDto);
    }

    // 브라우저의 beforeUnload 이벤트 리스너를 통해 Disconnect 메시지가 전송된다. (이때는 Unsubscribe 발생X)
    @EventListener
    @Transactional // 조회를 줄이기 위해 사용 //메모 1
    public void webSocketDisconnectListener(TwentyDisconnectEvent event) {
        User user = event.getUser();
        Long roomId = event.getRoomId();

        RoomMember member = memberService.findByRoomIdAndUserId(roomId, user.getId());
        if (member == null) { return; /* Unsubscribe 에서 이미 나간 유저 */ }
        
        member.setRoomConnected(false);
        boolean isPlayerDeleted = false; // 나간 플레이어가 관전자일 경우 false 유지
        if (member.isTwentyReady()) { // 게임중일때는 무조건 ready = true
            isPlayerDeleted = memberInfoService.deleteByUserId(user.getId());// 게임중 정보 제거
        }
        member.setTwentyReady(false);

        //LEGACY
            /*
        if (!memberService.existsMember(roomId, user.getId())) {
            return; // Unsubscribe 에서 이미 나감 처리된 유저
        }

        memberService.disconnectFromRoom(roomId, user.getId());
        memberService.twentyUnready(roomId, user.getId());

        // 게임중에 나가면, MemberInfo 삭제 (disconnect 가능)
        boolean isPlayerDeleted = false;
        if (roomService.findById(roomId).isGptActivated()) {
            isPlayerDeleted = memberInfoService.deleteByUserId(user.getId());
        }
             */

        TwentyMessageDto twentyMessageDto = TwentyMessageDto.createDisconnectMessage(event.getUser().getId(), user.getUsername(), isPlayerDeleted);
        messagingTemplate.convertAndSend("/topic/twenty-game/" + roomId, twentyMessageDto);

    }

    // 사용자가 직접 Unsubscribe 버튼 클릭하면 발생 한다. 이후 Disconnect 요청이 발생할 수있다(수정바람)
    @EventListener
    public void WebSocketUnsubscribeListener(TwentyUnsubscribeEvent event) {
        User user = event.getUser();
        Long roomId = event.getRoomId();
        RoomMember member = memberService.findByRoomIdAndUserId(roomId, user.getId());

        // 나간사람이 방장일때
        if (member.isRoomOwner()) {
            memberService.leaveRoomAllMember(roomId); // 방의 모든 member 삭제
            roomService.deleteById(roomId); // 방 삭제
            // 메시지는 일단 삭제하지 않음.
            TwentyMessageDto deleteRoomMessageDto = TwentyMessageDto.createRoomDeleteMessage();
            messagingTemplate.convertAndSend("/topic/twenty-game/" + roomId, deleteRoomMessageDto);
            return;
        }

        // 현재 접속한 사용자가 채팅방에서 나감
        memberService.leaveRoom(roomId, user.getId());

        TwentyMessageDto twentyMessageDto = TwentyMessageDto.createUnsubscribeMessage(user.getId(), user.getUsername());
        messagingTemplate.convertAndSend("/topic/twenty-game/" + roomId, twentyMessageDto);

    }

}
