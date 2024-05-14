package chat.twenty.service.lower;

import chat.twenty.domain.User;
import chat.twenty.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository repository;
    private final BCryptPasswordEncoder passwordEncoder;

    public User findById(Long id) {
        return repository.findById(id).orElse(null);
    }

    public Optional<User> findByLoginId(String loginId) {
        return repository.findByLoginId(loginId);
    }

    @Transactional
    public User save(User user) {
        // 비밀번호 암호화
        String rawPassword = user.getPassword();
        user.setPassword(passwordEncoder.encode(rawPassword));

        // 권한설정
        user.setRole("ROLE_USER");

        repository.save(user);
        return user;
    }

    @Transactional
    public Long updateUsername(Long id, User updateParam) {
        repository.findById(id).ifPresent(user -> {
            user.setUsername(updateParam.getUsername());
        });
        return id;
    }

    @Transactional
    public void deleteById(Long id) {
        repository.deleteById(id);
    }

    /**
     * 중복 체크
     * @return 존재하면(중복이면) true 반환
     */
    public boolean duplicateCheck(String option, String param) {
        return "username".equals(option) ?
                repository.findByUsername(param).isPresent() :
                repository.findByLoginId(param).isPresent();
    }

}
