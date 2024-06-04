package chat.twenty.repository;

import chat.twenty.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long>, ChatRoomRepositoryCustom{
    @Modifying @Query("update ChatRoom c set c.gptUuid = :gptUuid where c.id = :roomId")
    void setGptUuidById(Long roomId, String gptUuid);

    ChatRoom findByName(String roomName); // for flush test

}
