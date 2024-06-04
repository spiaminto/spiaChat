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

    @JsonManagedReference
    @OneToMany(mappedBy = "room")
    private List<RoomMember> members = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private ChatRoomType type;          // chatroom 타입

    private String name;                // chatroom 이름
    private boolean gptActivated;    // gpt 활성화 여부, db서 0 아니면 true 반환
    private String gptUuid;     // GPT activate 시의 세션구분. isGptOwner = true 일때 사용

    // 스무고개 옵션 ===================
    @Enumerated(EnumType.STRING)
    private TwentyGameSubject subject;  // 스무고개 주제

    private String customSubject; // 스무고개 직접입력한 주제, 폼 기본값 ""
    private String twentyAnswer; // 스무고개 답
    private int twentyNext; // 스무고개 다음 순서

    public ChatRoom(String name) {
        this.name = name;
        this.gptActivated = false;
    }

    /*
    column definition

    @Column(columnDefinition = "varchar(16)") private String name;                // chatroom 이름
    @Column(columnDefinition = "varchar(20)") @Enumerated(EnumType.STRING) private ChatRoomType type;          // chatroom 타입
    @Column(columnDefinition = "TINYINT(1)") private boolean gptActivated;    // gpt 활성화 여부, db서 0 아니면 true 반환
    @Column(columnDefinition = "varchar(8)") private String gptUuid;     // GPT activate 시의 세션구분. isGptOwner = true 일때 사용

    // 스무고개 옵션
    @Column(columnDefinition = "varchar(20)") @Enumerated(EnumType.STRING) private TwentyGameSubject subject;  // 스무고개 주제
    @Column(columnDefinition = "varchar(20)") private String customSubject; // 스무고개 직접입력한 주제, 폼 기본값 ""
    @Column(columnDefinition = "varchar(20)") private String twentyAnswer; // 스무고개 답
     */

}
