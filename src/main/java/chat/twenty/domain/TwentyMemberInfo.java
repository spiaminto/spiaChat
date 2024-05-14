package chat.twenty.domain;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.*;

/**
 * 스무고개 게임에서 RoomMember 의 상태를 관리
 * 게임 시작시 생성, 게임 종료시 삭제됨.
 */
@Getter @ToString @EqualsAndHashCode
@Entity
public class TwentyMemberInfo {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "twenty_id") private Long id;

    private Long userId;
    private Long roomId;
    @Column(columnDefinition = "TINYINT(1)")
    private boolean alive;
    private int twentyOrder;

    public static TwentyMemberInfo createNewMemberInfo(Long userId, Long roomId, int order) {
        TwentyMemberInfo memberInfo = new TwentyMemberInfo();
        memberInfo.userId = userId;
        memberInfo.roomId = roomId;
        memberInfo.alive = true;
        memberInfo.twentyOrder = order;
        return memberInfo;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }
}
