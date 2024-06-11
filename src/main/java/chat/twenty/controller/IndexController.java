package chat.twenty.controller;

import chat.twenty.auth.PrincipalDetails;
import chat.twenty.controller.form.RoomAddForm;
import chat.twenty.domain.ChatRoom;
import chat.twenty.domain.RoomMember;
import chat.twenty.domain.User;
import chat.twenty.dto.ChatRoomDto;
import chat.twenty.enums.ChatRoomType;
import chat.twenty.service.lower.ChatRoomService;
import chat.twenty.service.lower.RoomMemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class IndexController {
    private final ChatRoomService roomService;
    private final RoomMemberService memberService;

    @GetMapping("/ex") // 에러 테스트용
    public String errorTest(@RequestParam(required = false) String param) {
        throw new IllegalStateException("Example Exception with Param, param = {}" + param);
    }

    @RequestMapping("/")
    public String index(Model model,
                        @RequestParam(value = "needLogin", required = false) boolean needLogin,
                        @RequestParam(value = "isBanned", required = false) boolean isBanned,
                        @RequestParam(value = "isRoomDeleted", required = false) boolean isRoomDeleted
                        )throws InterruptedException {
        log.info("index() needLogin = {}, isBanned = {}, isRoomDeleted = {}", needLogin, isBanned, isRoomDeleted);

        Thread.sleep(100); // EventListener 작업 대기

        // 방 목록 + 접속중 카운트 조회
        List<ChatRoomDto> chatRoomDtoList = roomService.findRoomWithConnectedCount();
        model.addAttribute("roomList", chatRoomDtoList);

        if (isBanned) {
            model.addAttribute("alertMessage", "방장에 의해 강퇴되었습니다.");
        } else if (isRoomDeleted) {
            model.addAttribute("alertMessage", "방장에 의해 삭제되었거나, 존재하지 않는 방 입니다.");
        } if (needLogin) {
            model.addAttribute("needLogin", true);
        }

        RoomAddForm initRoomAddForm = new RoomAddForm();
        initRoomAddForm.setRoomType(ChatRoomType.CHAT);
        model.addAttribute("roomAddForm", initRoomAddForm);
        return "index";
    }

    @PostMapping("/room")
    public String createRoom(@Validated @ModelAttribute RoomAddForm roomAddForm,
                             BindingResult bindingResult, Model model,
                             @AuthenticationPrincipal PrincipalDetails principalDetails) {
//        log.info("chatroom = {}, currentUser = {}", roomAddForm, principalDetails.getUser());
        User currentUser = principalDetails.getUser();

        if (bindingResult.hasErrors()) {
            log.info("bindingResult = {}", bindingResult);
            List<ChatRoomDto> chatRoomDtoList = roomService.findRoomWithConnectedCount();
            model.addAttribute("roomList", chatRoomDtoList);
            model.addAttribute("isCreateRoomError", true);
            return "index";
        }

        // roomAddForm -> ChatRoom
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setName(roomAddForm.getName());
        chatRoom.setType(roomAddForm.getRoomType());
        chatRoom.setSubject(roomAddForm.getSubject());
        chatRoom.setCustomSubject(roomAddForm.getCustomSubject());

        // 방 작성
        ChatRoom savedRoom = roomService.save(chatRoom);

        // 방 멤버이자 오너로 입장
        memberService.enterRoom(savedRoom, currentUser.getId(), currentUser.getUsername(), true);

        return "redirect:/room/" + savedRoom.getId();
    }

    @GetMapping("/room/{roomId}")
    public String enterRoom(@PathVariable Long roomId, Model model,
                            @AuthenticationPrincipal PrincipalDetails principalDetails) throws InterruptedException {
//        log.info("enterRoom() roomId = {}", roomId);
        User currentUser = principalDetails.getUser();
        Thread.sleep(100); // EventListener 작업 대기 (LEAVE WHILE PLAYING 등)

        // room 정보 불러오기
        ChatRoom currentRoom = roomService.findRoomWithMembers(roomId);
        if (currentRoom == null) {
            return "redirect:/?isRoomDeleted=true"; // room 없음
        }

        // currentRoom 에 currentUser 가 속해 있는지 확인
        RoomMember findMember = currentRoom.getMembers().stream()
                .filter(roomMember -> roomMember.getUserId().equals(currentUser.getId()))
                .findFirst().orElse(null);
        if (findMember == null) {
            // 첫입장 처리
            memberService.enterRoom(currentRoom, currentUser.getId(),currentUser.getUsername(), false);
            findMember = memberService.findByRoomIdAndUserId(roomId, currentUser.getId());
        }

        model.addAttribute("currentMember", findMember);
        model.addAttribute("currentRoomId", roomId);
        model.addAttribute("chatRoom", currentRoom);

        ChatRoomType currentRoomType = currentRoom.getType();
        if (currentRoomType == ChatRoomType.CHAT) {
            return "room/chatRoom";
        } else {
            return "room/twentyGameRoom";
        }
    }

}
