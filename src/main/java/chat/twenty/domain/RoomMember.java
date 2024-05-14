package chat.twenty.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Entity
public class RoomMember {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id") private Long id;

    private Long userId; //  user 는 연관관계X

    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id") @ToString.Exclude @JsonBackReference
    private ChatRoom room;

    @Column(columnDefinition = "varchar(16)") private String username;    // 조회용 유저이름
    @Column(columnDefinition = "TINYINT(1)") private boolean roomConnected;
    @Column(columnDefinition = "TINYINT(1)") private boolean roomOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean gptOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean twentyReady;
    @Column(columnDefinition = "varchar(8)") private String gptUuid;     // GPT activate 시의 세션구분. isGptOwner = true 일때 사용

    /**
     * roomId, userId 로 RoomMember 생성
     */
//    public RoomMember(Long roomId, Long userId) {
//        this.userId = userId;
//        this.roomId = roomId;
//        this.roomConnected = true;
//        this.roomOwner = false;
//    }

    /**
     * roomId, userId, username 로 RoomMember 생성
     */
    public RoomMember(ChatRoom room, Long userId, String username) {
        this.userId = userId;
        this.username = username;
        this.room = room;
        this.roomConnected = true;
        this.roomOwner = false;
    }

}
