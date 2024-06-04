package chat.twenty.service.lower;

import chat.twenty.domain.ChatRoom;
import chat.twenty.dto.ChatRoomDto;
import chat.twenty.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository repository;
    @Transactional(readOnly = true)
    public ChatRoom findById(Long id) {
        return repository.findById(id).orElse(null);
    }
    @Transactional(readOnly = true)
    public List<ChatRoom> findAll() { return repository.findAll(); }

    @Transactional(readOnly = true)
    public String findTwentyAnswer(Long id) {
        return repository.findTwentyAnswerById(id);
    }

    @Transactional(readOnly = true)
    public ChatRoom findRoomWithMembers(Long id) {
        return repository.findRoomWithMembers(id);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomDto> findRoomWithConnectedCount() {
        return repository.findRoomWithConnectedCount();
    }

    public ChatRoom save(ChatRoom chatRoom) {
        repository.save(chatRoom);
        return chatRoom;
    }

    /**
     * 스무고개 순서 초기화
     */
    public void resetTwentyOrder(Long id) {
        repository.findById(id).ifPresent(chatRoom -> {
            log.info("currentTX = {}", TransactionSynchronizationManager.getCurrentTransactionName());
            chatRoom.setTwentyNext(0);
        });
    }


    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
