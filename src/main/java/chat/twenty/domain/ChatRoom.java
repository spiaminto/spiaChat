package chat.twenty.domain;

import chat.twenty.enums.ChatRoomType;
import chat.twenty.enums.TwentyGameSubject;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // chatroom id
    private String name;                // chatroom 이름
    private ChatRoomType type;          // chatroom 타입
    @Column(columnDefinition = "TINYINT(1)") // db 에서 0이 아니면 true 리턴
    private boolean gptActivated;    // gpt 활성화 여부 DB default false

    // 스무고개 옵션
    private TwentyGameSubject subject;  // 스무고개 주제
    private String customSubject; // 스무고개 직접입력한 주제, 폼 기본값 ""
    private int twentyNext; // 스무고개 다음 순서

    public ChatRoom(String name) {
        this.name = name;
        this.gptActivated = false;
    }

}
