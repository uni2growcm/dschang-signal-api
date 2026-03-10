package u2g.codylab.dschang_signal.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import u2g.codylab.dschang_signal.dto.ErrorResponseApiDTO;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        log.warn("Authorization denied: {}", ex.getMessage());

        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .message("You don't have permission to access this resource");

        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }


    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleErrorResponse(ErrorResponseException ex) {
        log.error("ErrorResponseException: {}", ex.getMessage(), ex);
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(ex.getStatus().value())
                .message(ex.getMessage());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseApiDTO> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal Server Error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}