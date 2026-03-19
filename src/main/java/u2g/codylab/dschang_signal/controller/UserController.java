package u2g.codylab.dschang_signal.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import u2g.codylab.dschang_signal.dto.ChangeRoleRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdatePasswordRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UpdateUserRequestApiDTO;
import u2g.codylab.dschang_signal.dto.UserApiDTO;
import u2g.codylab.dschang_signal.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserApiDTO>> getAllUsers(@PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to get all users");
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserApiDTO> getUserById(@PathVariable Long id) {
        log.debug("REST request to get user by id: {}", id);
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/me")
    public ResponseEntity<UserApiDTO> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to get current user");
        return ResponseEntity.ok(userService.getUserByEmail(userDetails.getUsername()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<UserApiDTO> updateUser(@PathVariable Long id,
                                                 @RequestBody UpdateUserRequestApiDTO request,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to update user: {}", id);
        return ResponseEntity.ok(userService.updateUser(id, request, userDetails.getUsername()));
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isCurrentUser(#id)")
    public ResponseEntity<Void> updatePassword(@PathVariable Long id,
                                               @RequestBody UpdatePasswordRequestApiDTO request,
                                               @AuthenticationPrincipal UserDetails userDetails) {
        log.debug("REST request to update password for user: {}", id);
        userService.updatePassword(id, request, userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserApiDTO> changeUserRole(@PathVariable Long id,
                                                     @RequestBody ChangeRoleRequestApiDTO request) {
        log.debug("REST request to change role for user: {}", id);
        return ResponseEntity.ok(userService.changeUserRole(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.debug("REST request to delete user: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}