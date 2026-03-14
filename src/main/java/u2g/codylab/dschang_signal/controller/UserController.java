package u2g.codylab.dschang_signal.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import u2g.codylab.dschang_signal.api.UserApi;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import java.util.List;

@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<List<UserApiDTO>> getAllUsers(Integer page, Integer size, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort));
        Page<UserApiDTO> users = userService.getAllUsers(pageable);
        return new ResponseEntity<>(users.getContent(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserApiDTO> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserApiDTO> changeUserRoleAdmin(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ChangeRoleRequestApiDTO changeRoleRequestApiDTO) {
        return ResponseEntity.ok(userService.changeUserRole(id, changeRoleRequestApiDTO));
    }
}