package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.ConflictException;
import u2g.codylab.dschang_signal.exception.UnauthorizedException;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void shouldRegisterUserSuccessfully() {

        RegisterRequestApiDTO dto = new RegisterRequestApiDTO();
        dto.setEmail("test@mail.com");
        dto.setPassword("password");
        dto.setFullName("Test User");

        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedPassword");

        authService.register(dto);

        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenEmailAlreadyExists() {

        RegisterRequestApiDTO dto = new RegisterRequestApiDTO();
        dto.setEmail("test@mail.com");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(new User()));

        assertThrows(ConflictException.class, () -> {
            authService.register(dto);
        });

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldLoginSuccessfully() {

        LoginRequestApiDTO dto = new LoginRequestApiDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("password");

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .thenReturn(true);

        User result = authService.login(dto);

        assertNotNull(result);
        assertEquals(dto.getEmail(), result.getEmail());
    }

    @Test
    void shouldThrowExceptionWhenPasswordInvalid() {

        LoginRequestApiDTO dto = new LoginRequestApiDTO();
        dto.setEmail("admin@test.com");
        dto.setPassword("wrongPassword");

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword("encodedPassword");

        when(userRepository.findByEmail(dto.getEmail()))
                .thenReturn(Optional.of(user));

        when(passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                .thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> {
            authService.login(dto);
        });
    }
}