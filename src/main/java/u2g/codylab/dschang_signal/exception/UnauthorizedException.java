package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ErrorResponseException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}