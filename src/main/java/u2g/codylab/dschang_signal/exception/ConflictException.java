package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class ConflictException extends ErrorResponseException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}