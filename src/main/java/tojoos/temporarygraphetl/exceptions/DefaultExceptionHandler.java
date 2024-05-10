package tojoos.temporarygraphetl.exceptions;

import java.time.LocalDateTime;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Default exception handler for the REST apis.
 *
 * @author Jan Olszówka
 * @version 1.0
 * @since 2024-05-10
 */
@ControllerAdvice
public class DefaultExceptionHandler {

  @ExceptionHandler(UserNotFoundException.class)
  public final ResponseEntity<Object> handleEntityNotFoundException(UserNotFoundException ex, HttpServletRequest request) {
    ApiError apiError = new ApiError(
        request.getRequestURI(),
        HttpStatus.NOT_FOUND,
        LocalDateTime.now(),
        ex.getMessage()
    );

    return new ResponseEntity<>(apiError, apiError.status());
  }
}