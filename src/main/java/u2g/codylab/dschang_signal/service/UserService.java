package u2g.codylab.dschang_signal.service;


import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.exception.BadRequestException;
import u2g.codylab.dschang_signal.mapper.UserMapper;
import u2g.codylab.dschang_signal.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
        log.debug("Request to etch user y id");
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("L'utilisateur avec l'ID " + id + " n'existe pas."));
        log.debug("User with id {} ound", user.getId());
        return userMapper.toUserDTO(user);
    }
}
