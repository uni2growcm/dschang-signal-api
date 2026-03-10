package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends ErrorResponseException {
    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
}