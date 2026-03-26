package u2g.codylab.dschang_signal.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;
import u2g.codylab.dschang_signal.entity.Role;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.ConflictException;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Transactional
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final I18nService i18nService;
    private final GoogleAuthService googleAuthService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       I18nService i18nService,
                       GoogleAuthService googleAuthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.i18nService = i18nService;
        this.googleAuthService = googleAuthService;
    }

    public void register(RegisterRequestApiDTO dto) {
        log.info("Registering user: {}", dto.getEmail());
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ConflictException(i18nService.get("register.error.email"));
        }
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setRole(Role.CITIZEN);
        user.setIsActive(true);
        user.setCreatedAt(Timestamp.from(Instant.now()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));
        user.setAuthProvider("LOCAL");
        userRepository.save(user);
        log.info("User registered: {}", dto.getEmail());
    }

    public User login(LoginRequestApiDTO dto) {
        log.info("Login attempt for: {}", dto.getEmail());
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new BadRequestException(i18nService.get("login.error.credentials")));
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException(i18nService.get("login.error.credentials"));
        }
        return user;
    }

    public User googleLogin(String googleToken) {
        log.info("Processing Google login");

        GoogleAuthService.GoogleUserInfo googleUser = googleAuthService.verifyToken(googleToken);

        Optional<User> existingUser = userRepository.findByEmail(googleUser.getEmail());

        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (user.getGoogleId() == null) {
                user.setGoogleId(googleUser.getGoogleId());
                user.setAuthProvider("GOOGLE");
                if (googleUser.getAvatar() != null) {
                    user.setAvatarUrl(googleUser.getAvatar());
                }
                userRepository.save(user);
                log.info("Google account linked to existing user: {}", googleUser.getEmail());
            }
            return user;
        }

        Optional<User> existingByGoogleId = userRepository.findByGoogleId(googleUser.getGoogleId());
        if (existingByGoogleId.isPresent()) {
            return existingByGoogleId.get();
        }

        log.info("Creating new user from Google: {}", googleUser.getEmail());
        User newUser = new User();
        newUser.setEmail(googleUser.getEmail());
        newUser.setFullName(googleUser.getFullName() != null ?
                googleUser.getFullName() :
                googleUser.getEmail().split("@")[0]);
        newUser.setGoogleId(googleUser.getGoogleId());
        newUser.setAvatarUrl(googleUser.getAvatar());
        newUser.setAuthProvider("GOOGLE");
        newUser.setRole(Role.CITIZEN);
        newUser.setIsActive(true);
        newUser.setCreatedAt(Timestamp.from(Instant.now()));
        newUser.setUpdatedAt(Timestamp.from(Instant.now()));
        newUser.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));

        return userRepository.save(newUser);
    }
}