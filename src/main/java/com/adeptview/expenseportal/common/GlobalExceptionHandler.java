package com.adeptview.expenseportal.common;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.HandlerMapping;
import org.xml.sax.SAXException;

/**
 * Turns exceptions into {@link ApiError} responses.
 *
 * Expected failures (bad input, missing records, access) are answered with a
 * message the user can act on. Anything else is logged in full, stack trace
 * included, and the user gets a generic message with the reference ID that
 * finds that log entry.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiError> notFound(NotFoundException e) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> badRequest(BadRequestException e) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> invalid(MethodArgumentNotValidException e) {
        FieldError first = e.getBindingResult().getFieldError();
        String message = first == null
                ? "The request is not valid"
                : first.getField() + ": " + first.getDefaultMessage();
        return respond(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MissingServletRequestPartException.class,
        MethodArgumentTypeMismatchException.class,
    })
    public ResponseEntity<ApiError> unreadable(Exception e) {
        return respond(HttpStatus.BAD_REQUEST, "The request is not valid");
    }

    @ExceptionHandler(SAXException.class)
    public ResponseEntity<ApiError> notXml(SAXException e) {
        return respond(HttpStatus.BAD_REQUEST, "The file is not valid XML");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tooLarge(MaxUploadSizeExceededException e) {
        return respond(HttpStatus.BAD_REQUEST, "The file is larger than 5 MB");
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiError> conflict(ConflictException e) {
        return respond(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> unauthenticated(AuthenticationException e) {
        return respond(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException e) {
        return respond(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> unexpected(Exception e, HttpServletRequest request) {
        Object route = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        log.error("Request failed: {} {}", request.getMethod(), route == null ? "(no route)" : route, e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Something went wrong");
    }

    private static ResponseEntity<ApiError> respond(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(message, MDC.get(RequestIdFilter.MDC_KEY)));
    }
}
