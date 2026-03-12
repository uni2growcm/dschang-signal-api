package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class ForbiddenException extends ErrorResponseException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}