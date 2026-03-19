package u2g.codylab.dschang_signal.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdatePasswordRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateUserRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.Role;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.ForbiddenException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.UserMapper;
import u2g.codylab.dschang_signal.repository.UserRepository;

import java.sql.Timestamp;
import java.time.Instant;

@Slf4j
@Transactional
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final I18nService i18nService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserMapper userMapper,
                       UserRepository userRepository,
                       I18nService i18nService,
                       PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
        this.i18nService = i18nService;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserApiDTO> getAllUsers(Pageable pageable) {
        log.debug("Request to get all Users");
        try {
            Page<UserApiDTO> dtos = userRepository.findAll(pageable)
                    .map(userMapper::toUserDTO);
            log.debug("Found {} users", dtos.getContent().size());
            return dtos;
        } catch (Exception e) {
            throw new BadRequestException("Invalid pagination parameters");
        }
    }

    public UserApiDTO getUserById(Long id) {
        log.debug("Request to fetch user by id");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFound", id)
                ));
        log.debug("User with id {} found", user.getId());
        return userMapper.toUserDTO(user);
    }

    public UserApiDTO getUserByEmail(String email) {
        log.debug("Request to fetch user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFoundEmail", email)
                ));
        log.debug("User with email {} found", user.getEmail());
        return userMapper.toUserDTO(user);
    }

    public User getUserEntityByEmail(String email) {
        log.debug("Request to fetch user entity by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFoundEmail", email)
                ));
    }

    public UserApiDTO updateUser(Long id, UpdateUserRequestApiDTO request, String currentUserEmail) {
        log.debug("Request to update user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFound", id)
                ));

        User currentUser = getUserEntityByEmail(currentUserEmail);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(id)) {
            throw new ForbiddenException(i18nService.get("user.error.forbidden"));
        }

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        user.setUpdatedAt(Timestamp.from(Instant.now()));

        User updatedUser = userRepository.save(user);
        log.debug("User with id {} updated", id);

        return userMapper.toUserDTO(updatedUser);
    }

    public void updatePassword(Long id, UpdatePasswordRequestApiDTO request, String currentUserEmail) {
        log.debug("Request to update password for user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFound", id)
                ));

        User currentUser = getUserEntityByEmail(currentUserEmail);

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(id)) {
            throw new ForbiddenException(i18nService.get("user.error.forbidden"));
        }

        if (!currentUser.getRole().equals(Role.ADMIN)) {
            if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
                throw new BadRequestException(i18nService.get("user.error.invalidCurrentPassword"));
            }
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(Timestamp.from(Instant.now()));

        userRepository.save(user);
        log.debug("Password updated for user with id: {}", id);
    }

    public UserApiDTO changeUserRole(Long id, ChangeRoleRequestApiDTO changeRoleRequestApiDTO) {
        log.debug("Request to change role of user with id {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFound", id)
                ));
        user.setRole(Role.valueOf(changeRoleRequestApiDTO.getRole().getValue()));
        User updatedUser = userRepository.save(user);
        log.debug("Role of user with id {} changed to {}", id, updatedUser.getRole());
        return userMapper.toUserDTO(updatedUser);
    }

    public void deleteUser(Long id) {
        log.debug("Request to delete user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        i18nService.get("user.error.notFound", id)
                ));

        userRepository.delete(user);
        log.debug("User with id {} deleted", id);
    }
}