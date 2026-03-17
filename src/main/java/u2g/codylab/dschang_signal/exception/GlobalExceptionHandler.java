package u2g.codylab.dschang_signal.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import u2g.codylab.dschang_signal.dto.ErrorResponseApiDTO;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

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

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleAuthenticationException(AuthenticationException ex) {
        log.warn("Authentication error: {}", ex.getMessage());
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Unauthorized");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleAuthNotFound(AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication not found: {}", ex.getMessage());
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.UNAUTHORIZED.value())
                .message("Authentication required");
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    // IMPORTANT : ErrorResponseException avant RuntimeException
    @ExceptionHandler(ErrorResponseException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleErrorResponse(ErrorResponseException ex) {
        log.error("ErrorResponseException: {}", ex.getMessage());
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(ex.getStatus().value())
                .message(ex.getMessage());
        return new ResponseEntity<>(error, ex.getStatus());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .message(message);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponseApiDTO> handleException(RuntimeException e) {
        log.error(e.getMessage());
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseApiDTO> handleGeneric(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage());
        ErrorResponseApiDTO error = new ErrorResponseApiDTO()
                .timestamp(OffsetDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Internal Server Error");
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}