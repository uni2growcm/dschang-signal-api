package u2g.codylab.dschang_signal.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.UserMapper;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private I18nService i18nService;

    @InjectMocks
    private UserService userService;

    // ─────────────────────────────────────────
    // updatePassword
    // ─────────────────────────────────────────

    @Test
    void shouldUpdatePasswordSuccessfully() {
        // Given
        String email           = "user@dschang.cm";
        String currentPassword = "oldPassword123";
        String newPassword     = "newPassword456";
        String encodedNew      = "encodedNewPassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedOldPassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn(encodedNew);
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.updatePassword(email, currentPassword, newPassword);


        assertEquals(encodedNew, user.getPassword());
        verify(userRepository).findByEmail(email);
        verify(passwordEncoder).matches(currentPassword, "encodedOldPassword");
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowBadRequestWhenCurrentPasswordIsIncorrect() {

        String email           = "user@dschang.cm";
        String currentPassword = "wrongPassword";
        String newPassword     = "newPassword456";

        User user = new User();
        user.setEmail(email);
        user.setPassword("encodedOldPassword");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(currentPassword, user.getPassword())).thenReturn(false);


        assertThrows(BadRequestException.class,
                () -> userService.updatePassword(email, currentPassword, newPassword));

        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundForPasswordUpdate() {

        String email = "unknown@dschang.cm";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(i18nService.get(any(), any())).thenReturn("User not found");


        assertThrows(NotFoundException.class,
                () -> userService.updatePassword(email, "any", "any"));

        verify(userRepository, never()).save(any());
    }

    // ─────────────────────────────────────────
    // updateProfile
    // ─────────────────────────────────────────

    @Test
    void shouldUpdateProfileSuccessfully() {

        String email    = "user@dschang.cm";
        String newEmail = "newemail@dschang.cm";
        String fullName = "Jean Paul";

        User user = new User();
        user.setEmail(email);
        user.setFullName("Old Name");

        UserApiDTO dto = new UserApiDTO();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(dto);


        UserApiDTO result = userService.updateProfile(email, newEmail, fullName);


        assertNotNull(result);
        assertEquals(newEmail, user.getEmail());
        assertEquals(fullName, user.getFullName());
        verify(userRepository).findByEmail(email);
        verify(userRepository).findByEmail(newEmail);
        verify(userRepository).save(user);
        verify(userMapper).toUserDTO(user);
    }

    @Test
    void shouldUpdateOnlyFullNameWhenEmailUnchanged() {

        String email    = "user@dschang.cm";
        String fullName = "New Full Name";

        User user = new User();
        user.setEmail(email);
        user.setFullName("Old Name");

        UserApiDTO dto = new UserApiDTO();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(dto);

        UserApiDTO result = userService.updateProfile(email, email, fullName);

        assertNotNull(result);
        assertEquals(email, user.getEmail()); // email inchangé
        assertEquals(fullName, user.getFullName());
        verify(userRepository, times(1)).findByEmail(email); // une seule fois
        verify(userRepository).save(user);
    }

    @Test
    void shouldThrowBadRequestWhenNewEmailAlreadyInUse() {

        String email    = "user@dschang.cm";
        String newEmail = "taken@dschang.cm";

        User user = new User();
        user.setEmail(email);

        User existingUser = new User();
        existingUser.setEmail(newEmail);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail(newEmail)).thenReturn(Optional.of(existingUser));


        assertThrows(BadRequestException.class,
                () -> userService.updateProfile(email, newEmail, "Any Name"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldThrowNotFoundWhenUserNotFoundForProfileUpdate() {

        String email = "unknown@dschang.cm";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(i18nService.get(any(), any())).thenReturn("User not found");


        assertThrows(NotFoundException.class,
                () -> userService.updateProfile(email, "new@email.cm", "Name"));

        verify(userRepository, never()).save(any());
    }

    @Test
    void shouldNotUpdateFullNameWhenBlank() {

        String email        = "user@dschang.cm";
        String originalName = "Jean Paul";

        User user = new User();
        user.setEmail(email);
        user.setFullName(originalName);

        UserApiDTO dto = new UserApiDTO();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(dto);


        userService.updateProfile(email, email, "   ");

        // Then
        assertEquals(originalName, user.getFullName());
        verify(userRepository).save(user);
    }
}