package u2g.codylab.dschang_signal.exception;

import org.springframework.http.HttpStatus;

public class PayloadTooLargeException extends ErrorResponseException {
    public PayloadTooLargeException(String message) {
        super(HttpStatus.PAYLOAD_TOO_LARGE, message);
    }
}