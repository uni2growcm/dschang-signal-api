package u2g.codylab.dschang_signal.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdatePasswordRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateUserRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.Role;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.exception.NotFoundException;
import u2g.codylab.dschang_signal.mapper.UserMapper;
import u2g.codylab.dschang_signal.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Timestamp;

@Slf4j
@Transactional
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;

    public UserService(UserMapper userMapper, UserRepository userRepository) {
        this.userMapper = userMapper;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public UserApiDTO getUserById(Long id) {
        log.debug("Request to fetch user by id");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("L'utilisateur avec l'ID " + id + " n'existe pas."));
        log.debug("User with id {} found", user.getId());
        return userMapper.toUserDTO(user);
    }

    @Transactional
    public UserApiDTO changeUserRole(Long id, ChangeRoleRequestApiDTO changeRoleRequestApiDTO) {
        log.debug("Request to change role of user with id {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'utilisateur avec l'ID " + id + " n'existe pas."
                ));
        user.setRole(Role.valueOf(changeRoleRequestApiDTO.getRole().getValue()));
        User updatedUser = userRepository.save(user);
        log.debug("Role of user with id {} changed to {}", id, updatedUser.getRole());
        return userMapper.toUserDTO(updatedUser);
    }

    public UserApiDTO getUserByEmail(String email) {
        log.debug("Request to fetch user by email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
        log.debug("User with email {} found", user.getEmail());
        return userMapper.toUserDTO(user);
    }

    public User getUserEntityByEmail(String email) {
        log.debug("Request to fetch user entity by email: {}", email);
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found with email: " + email));
    }

    @Transactional
    public UserApiDTO updateUser(Long id, UpdateUserRequestApiDTO request, String currentUserEmail) {
        log.debug("Request to update user with id {}", id);

        User currentUser = getUserEntityByEmail(currentUserEmail);
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only update your own profile"
            );
        }

        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found"
                ));

        if (request.getEmail() != null && !request.getEmail().equals(userToUpdate.getEmail())) {
            userRepository.findByEmail(request.getEmail()).ifPresent(existingUser -> {
                if (!existingUser.getId().equals(id)) {
                    throw new ResponseStatusException(
                            HttpStatus.CONFLICT,
                            "Email already in use"
                    );
                }
            });
            userToUpdate.setEmail(request.getEmail());
        }

        if (request.getFullName() != null) {
            userToUpdate.setFullName(request.getFullName());
        }

        userToUpdate.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        User updatedUser = userRepository.save(userToUpdate);
        log.debug("User with id {} updated successfully", id);

        return userMapper.toUserDTO(updatedUser);
    }

    @Transactional
    public void updatePassword(Long id, UpdatePasswordRequestApiDTO request, String currentUserEmail) {
        log.debug("Request to update password for user with id {}", id);

        User currentUser = getUserEntityByEmail(currentUserEmail);
        if (!currentUser.getId().equals(id)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You can only update your own password"
            );
        }

        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User with id " + id + " not found"
                ));

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        if (!passwordEncoder.matches(request.getCurrentPassword(), userToUpdate.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Current password is incorrect"
            );
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "New password and confirmation do not match"
            );
        }

        userToUpdate.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userToUpdate.setUpdatedAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(userToUpdate);
        log.debug("Password updated successfully for user with id {}", id);
    }
}