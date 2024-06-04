package chat.twenty.repository;

import chat.twenty.domain.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
    List<RoomMember> findByRoomId(Long roomId);
    RoomMember findByRoomIdAndUserId(Long roomId, Long userId);

    @Query("Select m FROM RoomMember m WHERE m.room.id = :roomId AND m.roomOwner = true")
    RoomMember findRoomOwnerByRoomId(@Param("roomId") Long roomId);

    List<RoomMember> findByRoomIdAndTwentyReady(Long roomId, Boolean twentyReady);
    RoomMember findByUsername(String username);

    boolean existsByRoomIdAndUserId(Long roomId, Long userId);

    long countByRoomId(Long roomId);
    long countByUserId(Long userId); //flushTest
    long countByRoomIdAndTwentyReady(Long roomId, Boolean twentyReady);
    long countByRoomIdAndRoomConnected(Long roomId, Boolean roomConnected);

    /**
     * RoomMember.roomConnected 조회없이 바로 업데이트
     */
    @Modifying @Query("update RoomMember m set m.roomConnected = :roomConnected where m.room.id = :roomId and m.userId = :userId")
    void setConnectedByRoomIdAndUserId(Long roomId, Long userId, boolean roomConnected);

    /**
     * RoomMember.twentyReady 조회없이 바로 업데이트
     */
    @Modifying @Query("update RoomMember m set m.twentyReady = :twentyReady where m.room.id = :roomId and m.userId = :userId")
    void setTwentyReadyByRoomIdAndUserId(Long roomId, Long userId, boolean twentyReady);

    /**
     * 여러개의 RoomMember.twentyReady 조회 없이 바로 업데이트
     */
    @Modifying @Query("update RoomMember m set m.twentyReady = :twentyReady where m.room.id = :roomId")
    void setTwentyReadyAllByRoomId(Long roomId, boolean twentyReady);

    void deleteByRoomIdAndUserId(Long roomId, Long userId);
    void deleteByRoomId(Long roomId);

}
