package u2g.codylab.dschang_signal.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponseException extends RuntimeException {

    private final HttpStatus status;
    private final String message;

    public ErrorResponseException(HttpStatus status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }
}