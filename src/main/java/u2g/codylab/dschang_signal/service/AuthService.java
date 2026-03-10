package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;
import u2g.codylab.dschang_signal.entity.Role;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Transactional
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(RegisterRequestApiDTO dto) {
        log.info("Registering user: {}", dto.getEmail());
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new RuntimeException("Email already in use: " + dto.getEmail());
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole(Role.CITIZEN);
        user.setIsActive(true);
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        userRepository.save(user);
        log.info("User registered: {}", dto.getEmail());
    }

    public User login(LoginRequestApiDTO dto) {
        log.info("Login attempt for: {}", dto.getEmail());
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }
}