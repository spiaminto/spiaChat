package chat.twenty.service.lower;

import chat.twenty.domain.ChatMessage;
import chat.twenty.repository.ChatMessageRepository;
import chat.twenty.repository.LegacyChatMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ChatMessageService {

    private final ChatMessageRepository repository;

    @Transactional
    public ChatMessage saveMessage(ChatMessage chatMessage) {

        return repository.save(chatMessage);
    }


    public ChatMessage findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public List<ChatMessage> findAll() {
        return repository.findAll();
    }

    public List<ChatMessage> findCurrentGptQueue(Long roomId, String gptUuid) {
        return repository.findGptChatByRoomIdAndGptUuid(roomId, gptUuid);
    }

    public List<ChatMessage> findByRoomId(Long roomId) {
        return repository.findByRoomId(roomId);
    }

    public List<ChatMessage> findByRoomIdAndUserId(Long roomId, Long userId) {
        return repository.findByRoomIdAndUserId(roomId, userId);
    }

    // 메시지 수정과 삭제는 우선 구현하지 않도록 함.

}
