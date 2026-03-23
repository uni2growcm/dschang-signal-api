package u2g.codylab.dschang_signal.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import u2g.codylab.dschang_signal.api.UserApi;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdatePasswordRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateProfileRequestApiDTO;
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

        HttpHeaders headers = new HttpHeaders();
        headers.add("X-Total-Count",  String.valueOf(usersPage.getTotalElements()));
        headers.add("X-Page-Size",    String.valueOf(usersPage.getSize()));
        headers.add("X-Page-Number",  String.valueOf(usersPage.getNumber()));

        return new ResponseEntity<>(usersPage.getContent(),headers, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<UserApiDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserApiDTO> changeUserRole(@PathVariable Long id,
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
    public ResponseEntity<Void> updatePassword(
            @Valid @RequestBody UpdatePasswordRequestApiDTO updatePasswordRequestApiDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        userService.updatePassword(
                email,
                updatePasswordRequestApiDTO.getCurrentPassword(),
                updatePasswordRequestApiDTO.getNewPassword()
        );

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<UserApiDTO> updateProfile(
            @Valid @RequestBody UpdateProfileRequestApiDTO updateProfileRequestApiDTO) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return ResponseEntity.ok(userService.updateProfile(
                email,
                updateProfileRequestApiDTO.getEmail(),
                updateProfileRequestApiDTO.getFullName()
        ));
    }

}