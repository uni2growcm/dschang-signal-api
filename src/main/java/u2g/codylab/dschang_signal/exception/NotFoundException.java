package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends ErrorResponseException {
    public NotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}