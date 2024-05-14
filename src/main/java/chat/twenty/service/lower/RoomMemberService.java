package chat.twenty.service.lower;

import chat.twenty.domain.ChatRoom;
import chat.twenty.domain.RoomMember;
import chat.twenty.repository.ChatRoomRepository;
import chat.twenty.repository.RoomMemberRepository;
import chat.twenty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class RoomMemberService {

    private final UserRepository userRepository;
    private final RoomMemberRepository repository;
    private final ChatRoomRepository roomRepository;

    @Transactional(readOnly = true)
    public RoomMember findByRoomIdAndUserId(Long roomId, Long userId) {
        return repository.findByRoomIdAndUserId(roomId, userId);
    }
    @Transactional(readOnly = true)
    public RoomMember findRoomOwner(Long roomId) {
        return repository.findRoomOwnerByRoomId(roomId);
    }
    @Transactional(readOnly = true)
    public List<RoomMember> findRoomMembers(Long roomId) {
        return repository.findByRoomId(roomId);
    }

    public Boolean existsMember(Long roomId, Long userId) {
        return repository.existsByRoomIdAndUserId(roomId, userId);
    }
    @Transactional(readOnly = true)
    public long countRoomMember(Long roomId) {
        return repository.countByRoomId(roomId);
    }
    @Transactional(readOnly = true)
    public long countConnectedMember(Long roomId) {
        return repository.countByRoomIdAndRoomConnected(roomId, true);
    }

    // 입장/퇴장/접속 ======================================================================
    /**
     * 방에 첫 입장
     * @param isOwner 방장 여부
     */
    public void enterRoom(Long roomId, Long userId, boolean isOwner) {
        ChatRoom room = roomRepository.findById(roomId).orElse(null);
        RoomMember roomMember = new RoomMember(room, userId, userRepository.findById(userId).get().getUsername());
        if (isOwner) roomMember.setRoomOwner(true);
        repository.save(roomMember);
    }

    /**
     * 방에서 퇴장
     */
    public void leaveRoom(Long roomId, Long userId) {
        repository.deleteByRoomIdAndUserId(roomId, userId);
    }
    public void leaveRoomAllMember(Long roomId) {
        repository.deleteByRoomId(roomId);
    }

    /**
     * 유저를 방에 connect
     */
    public void connectToRoom(Long roomId, Long userId) {
        repository.findByRoomIdAndUserId(roomId, userId).setRoomConnected(true);
    }

    /**
     * 유저를 방에서 disconnect
     */
    public void disconnectFromRoom(Long roomId, Long userId) {
        repository.findByRoomIdAndUserId(roomId, userId).setRoomConnected(false);
    }

    // GPT ======================================================================

    public String findGptUuidByRoomId(Long roomId) {
        return repository.findGptUuidByRoomId(roomId).orElse(null);
    }
    public void updateGptUuid(Long roomId, Long userId, String gptUuid) {
        repository.findByRoomIdAndUserId(roomId, userId).setGptUuid(gptUuid);
    }
    /**
     * gptOwner 설정 후 해당 유저의 id 반환
     */
    public Long updateGptOwner(Long roomId, Long userId, boolean gptOwner) {
        repository.findByRoomIdAndUserId(roomId, userId).setGptOwner(gptOwner);
        return userId;
    }

    // 스무고개 ======================================================================
    public List<RoomMember> findTwentyReadyMembersByRoomId(Long roomId) {
        return repository.findByRoomIdAndTwentyReady(roomId, true);
    }
    public long countTwentyReadyMemberByRoomId(Long roomId) {
        return repository.countByRoomIdAndTwentyReady(roomId, true);
    }

    public void twentyReady(Long roomId, Long userId) {
        repository.findByRoomIdAndUserId(roomId, userId).setTwentyReady(true);
    }
    public void twentyUnready(Long roomId, Long userId) {
        repository.findByRoomIdAndUserId(roomId, userId).setTwentyReady(false);
    }
    public void twentyUnreadyAllMember(Long roomId) {
        repository.findByRoomId(roomId).forEach(roomMember -> roomMember.setTwentyReady(false));
    }
    public boolean isTwentyAllReady(Long roomId) {
        return countTwentyReadyMemberByRoomId(roomId) == countRoomMember(roomId);
    }

}
