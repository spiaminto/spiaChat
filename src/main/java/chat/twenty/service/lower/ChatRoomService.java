package chat.twenty.service.lower;

import chat.twenty.domain.ChatRoom;
import chat.twenty.repository.ChatRoomRepository;
import chat.twenty.repository.LegacyChatRoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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


    public ChatRoom save(ChatRoom chatRoom) {
        repository.save(chatRoom);
        return chatRoom;
    }

    /**
     * 미구현
     */

    public Long update(Long id, ChatRoom updateParam) {
        return id;
    }


    public void updateGptActivated(Long id, boolean isGptActivated) {
        repository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setGptActivated(isGptActivated);
        });
    }

    /**
     * 스무고개 순서 업데이트
     */

    public void updateNextTwentyOrder(Long id, int twentyNext) {
        repository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setTwentyNext(twentyNext);
        });
    }

    /**
     * 스무고개 순서 초기화
     */

    public void resetTwentyOrder(Long id) {
        repository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setTwentyNext(0);
        });
    }


    public void setTwentyAnswer(Long id, String twentyAnswer) {
        repository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setTwentyAnswer(twentyAnswer);
        });
    }

    public void removeTwentyAnswer(Long id) {
        repository.findById(id).ifPresent(chatRoom -> {
            chatRoom.setTwentyAnswer(null);
        });
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
