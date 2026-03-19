package u2g.codylab.dschang_signal.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import u2g.codylab.dschang_signal.api.AuthApi;
import u2g.codylab.dschang_signal.dto.AuthResponseApiDTO;
import u2g.codylab.dschang_signal.dto.LoginRequestApiDTO;
import u2g.codylab.dschang_signal.dto.RegisterRequestApiDTO;
import u2g.codylab.dschang_signal.entity.User;
import u2g.codylab.dschang_signal.service.AuthService;
import u2g.codylab.dschang_signal.service.JwtService;
import u2g.codylab.dschang_signal.service.TokenBlacklistService;

@Slf4j
@RestController
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthController(AuthService authService,
                          JwtService jwtService,
                          TokenBlacklistService tokenBlacklistService) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    public ResponseEntity<AuthResponseApiDTO> login(LoginRequestApiDTO loginRequestApiDTO) {
        User user = authService.login(loginRequestApiDTO);
        String token = jwtService.generateToken(user.getEmail());
        tokenBlacklistService.registerActiveToken(user.getEmail(), token);
        AuthResponseApiDTO response = new AuthResponseApiDTO();
        response.setToken(token);
        return ResponseEntity.ok(response);
    }

    @Override
    public ResponseEntity<Void> register(RegisterRequestApiDTO registerRequestApiDTO) {
        authService.register(registerRequestApiDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}