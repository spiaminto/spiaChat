package chat.twenty.repository;

import chat.twenty.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomId(Long roomId);
    List<ChatMessage> findByRoomIdAndUserId(Long roomId, Long userId);

    @Query("SELECT c FROM ChatMessage c WHERE c.roomId = :roomId AND c.gptUuid = :gptUuid AND c.gptChat = true")
    List<ChatMessage> findGptChatByRoomIdAndGptUuid(@Param("roomId") Long roomId, @Param("gptUuid") String gptUuid);

}
