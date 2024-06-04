package chat.twenty.repository;

import chat.twenty.domain.ChatRoom;
import chat.twenty.dto.ChatRoomDto;

import java.util.List;

public interface ChatRoomRepositoryCustom {
    String findTwentyAnswerById(Long id);
    ChatRoom findRoomWithMembers(Long Id);
    List<ChatRoomDto> findRoomWithConnectedCount();
}
