package chat.twenty.service.lower;

import chat.twenty.domain.TwentyMemberInfo;
import chat.twenty.repository.TwentyMemberInfoRepository;
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
public class TwentyMemberInfoService {
    private final TwentyMemberInfoRepository repository;
    @Transactional(readOnly = true)
    public TwentyMemberInfo findByUserId(Long userId) {
        return repository.findByUserId(userId);
    }
    @Transactional(readOnly = true)
    public List<TwentyMemberInfo> findByRoomId(Long roomId) {
        return repository.findByRoomId(roomId);
    }
    @Transactional(readOnly = true)
    public TwentyMemberInfo findByRoomIdAndOrder(Long roomId, int order) {
        return repository.findByRoomIdAndTwentyOrder(roomId, order);
    }
    @Transactional(readOnly = true)
    public long countByRoomId(Long roomId) {
        return repository.countByRoomId(roomId);
    }
    @Transactional(readOnly = true)
    public boolean isRoomAllDead(Long roomId) {
        return repository.countByRoomIdAndAlive(roomId, true) == 0;
    }

    public TwentyMemberInfo save(TwentyMemberInfo twentyMemberInfo) {
        repository.save(twentyMemberInfo);
        return twentyMemberInfo;
    }

    /**
     * 한 멤버의 생존여부를 업데이트 (멤버는 한 게임에만 참여할 수 있음)
     * @param userId
     */
    public void updateMemberAlive(Long userId, boolean isAlive) {
         repository.findByUserId(userId).setAlive(isAlive);
    }

    /**
     * 방의 모든 멤버의 생존 여부를 한번에 업데이트
     * @param roomId
     */
    public void updateMembersAlive(Long roomId, boolean isAlive) {
        repository.findByRoomId(roomId).forEach(twentyMemberInfo -> {
            twentyMemberInfo.setAlive(isAlive);
        });
    }

    /**
     * 게임중 멤버 나가면 삭제
     */
    public boolean deleteByUserId(Long userId) {
        return repository.deleteByUserId(userId) == 1;
    }

    public long deleteByRoomId(Long roomId) {
        return repository.deleteByRoomId(roomId);
    }
    public long deleteAllByRoomId(Long roomId) {
        return repository.deleteAllByRoomId(roomId);
    }

}
