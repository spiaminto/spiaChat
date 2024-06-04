package chat.twenty.repository;

import chat.twenty.domain.TwentyMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TwentyMemberInfoRepository extends JpaRepository<TwentyMemberInfo, Long>{
    List<TwentyMemberInfo> findByRoomId(Long roomId);
    TwentyMemberInfo findByUserId(Long userId);
    TwentyMemberInfo findByRoomIdAndTwentyOrder(Long roomId, Integer twentyOrder);
    long countByRoomId(Long roomId);
    long countByRoomIdAndAlive(Long roomId, Boolean alive);
    long deleteByUserId(Long userId);
    long deleteByRoomId(Long roomId);

    /**
     * 게임 종료시 전 멤버 인포 삭제
     */
    @Modifying @Query("delete from TwentyMemberInfo t where t.roomId = :roomId")
    int deleteAllByRoomId(Long roomId);

    @Modifying @Query("update TwentyMemberInfo t set t.alive = :alive where t.userId = :userId")
    int updateAliveByUserId(Long userId, Boolean alive); // flush test

}
