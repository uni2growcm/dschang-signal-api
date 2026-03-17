package u2g.codylab.dschang_signal.controller;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import u2g.codylab.dschang_signal.api.UserApi;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdatePasswordRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateUserRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController implements UserApi {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<List<UserApiDTO>> getAllUsers(Integer page, Integer size, String sort) {

        int p = (page != null) ? page : 0;
        int s = (size != null) ? size : 20;
        String st = (sort != null) ? sort : "id";

        Pageable pageable = PageRequest.of(p, s, Sort.by(st));
        Page<UserApiDTO> usersPage = userService.getAllUsers(pageable);
        return new ResponseEntity<>(usersPage.getContent(), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserApiDTO> getUserById(Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserApiDTO> changeUserRoleAdmin(@PathVariable("id") Long id,
                                                          @Valid @RequestBody ChangeRoleRequestApiDTO changeRoleRequestApiDTO) {
        return ResponseEntity.ok(userService.changeUserRole(id, changeRoleRequestApiDTO));
    }

    @Override
    public ResponseEntity<UserApiDTO> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @Override
    public ResponseEntity<UserApiDTO> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequestApiDTO updateUserRequestApiDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        UserApiDTO updatedUser = userService.updateUser(id, updateUserRequestApiDTO, currentUserEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @Override
    public ResponseEntity<Void> updatePassword(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdatePasswordRequestApiDTO updatePasswordRequestApiDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        userService.updatePassword(id, updatePasswordRequestApiDTO, currentUserEmail);
        return ResponseEntity.ok().build();
    }
}