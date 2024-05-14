package chat.twenty.domain;

import chat.twenty.enums.ChatRoomType;
import chat.twenty.enums.TwentyGameSubject;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id") private Long id;                    // chatroom id

    @OneToMany(mappedBy = "room")
    @JsonManagedReference
    private List<RoomMember> members = new ArrayList<>();

    @Column(columnDefinition = "varchar(16)") private String name;                // chatroom 이름
    @Column(columnDefinition = "varchar(20)") @Enumerated(EnumType.STRING) private ChatRoomType type;          // chatroom 타입
    @Column(columnDefinition = "TINYINT(1)") private boolean gptActivated;    // gpt 활성화 여부, db서 0 아니면 true 반환

    // 스무고개 옵션
    @Column(columnDefinition = "varchar(20)") @Enumerated(EnumType.STRING) private TwentyGameSubject subject;  // 스무고개 주제
    @Column(columnDefinition = "varchar(20)") private String customSubject; // 스무고개 직접입력한 주제, 폼 기본값 ""
    @Column(columnDefinition = "varchar(20)") private String twentyAnswer; // 스무고개 답
    private int twentyNext; // 스무고개 다음 순서

    public ChatRoom(String name) {
        this.name = name;
        this.gptActivated = false;
    }

}
