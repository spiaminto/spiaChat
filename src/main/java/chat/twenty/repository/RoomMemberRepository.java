package chat.twenty.repository;

import chat.twenty.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    List<RoomMember> findByRoomId(Long roomId);
    RoomMember findByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    @Query("SELECT m.gptUuid FROM RoomMember m WHERE m.id = :roomId")
    Optional<String> findGptUuidByRoomId(Long roomId);

    @Query("Select m FROM RoomMember m WHERE m.id = :roomId AND m.roomOwner = true")
    RoomMember findRoomOwnerByRoomId(Long roomId);

    List<RoomMember> findByRoomIdAndTwentyReady(Long roomId, Boolean twentyReady);

    boolean existsByRoomIdAndUserId(@Param("roomId") Long roomId, @Param("userId") Long userId);

    long countByRoomId(Long roomId);
    long countByRoomIdAndTwentyReady(Long roomId, Boolean twentyReady);
    long countByRoomIdAndRoomConnected(Long roomId, Boolean roomConnected);

    void deleteByRoomIdAndUserId(Long roomId, Long userId);
    void deleteByRoomId(Long roomId);
}
