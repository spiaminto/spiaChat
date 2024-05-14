package chat.twenty.repository;

import chat.twenty.domain.TwentyMemberInfo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TwentyMemberInfoRepository extends JpaRepository<TwentyMemberInfo, Long>{
    List<TwentyMemberInfo> findByRoomId(Long roomId);
    TwentyMemberInfo findByUserId(Long userId);
    TwentyMemberInfo findByRoomIdAndTwentyOrder(Long roomId, Integer twentyOrder);
    long countByRoomIdAndAlive(Long roomId, Boolean alive);
    long deleteByUserId(Long userId);
    long deleteByRoomId(Long roomId);

}
