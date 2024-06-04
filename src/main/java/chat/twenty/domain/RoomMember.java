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

    private Long userId; //  user_id idx

    @ToString.Exclude @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "room_id")
    private ChatRoom room; // room_id idx

    private String username;
    private boolean roomConnected;
    private boolean roomOwner;
    private boolean gptOwner;
    private boolean twentyReady;

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

    /*
     column definition
     @Column(columnDefinition = "varchar(16)") private String username;    // 조회용 유저이름
    @Column(columnDefinition = "TINYINT(1)") private boolean roomConnected;
    @Column(columnDefinition = "TINYINT(1)") private boolean roomOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean gptOwner;
    @Column(columnDefinition = "TINYINT(1)") private boolean twentyReady;
     */

}
