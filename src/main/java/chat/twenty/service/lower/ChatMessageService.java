package chat.twenty.service.lower;

import chat.twenty.domain.ChatMessage;
import chat.twenty.repository.LegacyChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatMessageService {

    private final LegacyChatMessageRepository legacyChatMessageRepository;

    public ChatMessage saveMessage(ChatMessage chatMessage) {

        return legacyChatMessageRepository.save(chatMessage);
    }

    public ChatMessage findById(Long id) {
        return legacyChatMessageRepository.findById(id);
    }

    public List<ChatMessage> findAll() {
        return legacyChatMessageRepository.findAll();
    }
    public List<ChatMessage> findCurrentGptQueue(Long roomId, String gptUuid) {
        return legacyChatMessageRepository.findGptChatByRoomIdAndGptUuid(roomId, gptUuid);
    }

    public List<ChatMessage> findByRoomId(Long roomId) {
        return legacyChatMessageRepository.findByRoomId(roomId);
    }

    public List<ChatMessage> findByRoomIdAndUserId(Long roomId, Long userId) {
        return legacyChatMessageRepository.findByRoomIdAndUserId(roomId, userId);
    }

    // 메시지 수정과 삭제는 우선 구현하지 않도록 함.

}
