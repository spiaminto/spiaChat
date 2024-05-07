package chat.twenty.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class RoomMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id") private Long id;

    private Long userId; //  user 는 연관관계X
    @Transient Long roomId; // 개발용

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id")
    private ChatRoom chatRoom;

    @Column(columnDefinition = "varchar(16)") private String username;    // 조회용 유저이름
    @Column(columnDefinition = "TINYINT(1)") private boolean roomConnected;
    @Column(columnDefinition = "TINYINT(1)") private boolean roomOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean gptOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean twentyGameReady;
    @Column(columnDefinition = "varchar(8)") private String gptUuid;     // GPT activate 시의 세션구분. isGptOwner = true 일때 사용

    /**
     * roomId, userId 로 RoomMember 생성
     */
    public RoomMember(Long roomId, Long userId) {
        this.userId = userId;
        this.roomId = roomId;
        this.roomConnected = true;
    }

    /**
     * roomId, userId, username 로 RoomMember 생성
     */
    public RoomMember(Long roomId, Long userId, String username) {
        this.userId = userId;
        this.roomId = roomId;
        this.username = username;
        this.roomConnected = true;
    }

}
