package chat.twenty.domain;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Entity
@NoArgsConstructor
public class User {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") private Long id;            // id,  auto_increment

    private String username;    // 유저이름 16
    private String loginId;     // 로그인 아이디 16, UNIQUE(index)
    private String password;    // 비밀번호 Bcrypt round10, varchar100
    private String role;        // 권한 "ROLE_USER", "ROLE_ADMIN"

    public User(String username) {
        this.username = username;
    }

    /**
     * UserAddForm -> User
     */
    public User(String loginId, String username, String password) {
        this.loginId = loginId;
        this.username = username;
        this.password = password;
    }

    /*
    column definition
    @Column(columnDefinition = "varchar(16)") private String username;    // 유저이름 16
    @Column(columnDefinition = "varchar(16)") private String loginId;     // 로그인 아이디 16
    @Column(columnDefinition = "varchar(100)") private String password;    // 비밀번호 Bcrypt round10, varchar100
    @Column(columnDefinition = "varchar(30)") private String role;        // 권한 "ROLE_USER", "ROLE_ADMIN"
     */

}
