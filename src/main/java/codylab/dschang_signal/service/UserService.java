package codylab.dschang_signal.service;

import codylab.dschang_signal.UserRepository.UserRepository;
import codylab.dschang_signal.entity.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "L'utilisateur avec l'ID " + id + " n'existe pas."
                ));
    }
}