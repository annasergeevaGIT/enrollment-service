package at.enrollment_service.exception;

import jakarta.servlet.http.HttpServletRequest; // 1. Import blocking request type
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException; // 2. Standard WebMVC exception for @RequestBody validation errors
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.context.request.WebRequest; // Used for a more generic request context
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.net.URI;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getGlobalErrors().forEach(e ->
                errors.put(e.getObjectName(), e.getDefaultMessage()));

        ex.getBindingResult().getFieldErrors().forEach(e ->
                errors.put(e.getField(), e.getDefaultMessage()));

        log.error("Intercepted validation exception. Errors: {}", errors);

        ProblemDetail pd = createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);
        pd.setProperty("invalid_params", errors);

        return new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ProblemDetail> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        log.error("Intercepted HttpMessageNotReadableException. Message: {}", ex.getMessage());

        ProblemDetail pd = createProblemDetail(ex.getMessage(), HttpStatus.BAD_REQUEST, request);

        return new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ProblemDetail> handleHandlerMethodValidationException(
            HandlerMethodValidationException ex,
            HttpServletRequest request
    ) {

        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        Map<String, String> errors = new HashMap<>();

        ex.getParameterValidationResults().forEach(result ->
                result.getResolvableErrors().forEach(e ->
                        errors.put(result.getMethodParameter().getParameterName(), e.getDefaultMessage())
                )
        );

        log.error("Intercepted HandlerMethodValidationException. Errors: {}", errors);

        pd.setProperty("invalid_params", errors);
        pd.setInstance(createUri(request));

        return new ResponseEntity<>(pd, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(EnrollmentServiceException.class)
    public ResponseEntity<ProblemDetail> handleEnrollmentServiceException(
            EnrollmentServiceException ex,
            HttpServletRequest request) {

        log.error("Intercepted EnrollmentServiceException. Status: {}, Message: {}",
                ex.getStatus(), ex.getMessage());

        ProblemDetail pd = createProblemDetail(ex.getMessage(), ex.getStatus(), request);

        return new ResponseEntity<>(pd, ex.getStatus());
    }

    private static ProblemDetail createProblemDetail(
            String message,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, message);
        pd.setProperty("timestamp", Instant.now());
        pd.setInstance(createUri(request));
        return pd;
    }

    private static URI createUri(HttpServletRequest request) {
        try {
            return URI.create(request.getRequestURL().toString());
        } catch (Exception e) {
            return null;
        }
    }
}
