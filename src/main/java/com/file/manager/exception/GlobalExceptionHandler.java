package com.file.manager.exception;

import com.file.manager.dto.ErrorResponse;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // For specific exceptions (example: IllegalArgumentException)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AuthExcpetion.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthExcpetion ex) {
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                "AUTH_ERROR",                   // set your message code explicitly here
                ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(FileSystemException.class)
    public ResponseEntity<ErrorResponse> handleFileSystemException(FileSystemException ex){
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                "FILESYSTEM_ERROR",                   // set your message code explicitly here
                ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }   @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequestException(BadRequestException ex){
        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                "FILESYSTEM_ERROR",                   // set your message code explicitly here
                ex.getClass().getSimpleName(),
                HttpStatus.BAD_REQUEST.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse<String,String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String msg = error.getDefaultMessage();
            errors.put(field, msg);
        });

        ErrorResponse<String,String> response = new ErrorResponse<>(
                "Validation failed for one or more fields",
                "VALIDATION_ERROR",
                "Bad Request",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse<?,?>> handleAllExceptions(Exception ex) {

        ErrorResponse response = new ErrorResponse(
                ex.getMessage(),
                ex.getMessage(),
                ex.getClass().getSimpleName(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                null
        );
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Add more @ExceptionHandler methods for specific exceptions as needed

}
