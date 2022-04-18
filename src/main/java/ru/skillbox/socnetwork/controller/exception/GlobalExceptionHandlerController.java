package ru.skillbox.socnetwork.controller.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandlerController {

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidRequestException(InvalidRequestException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponseDto(ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDto> handleHttpMessageNotReadableException() {
        return ResponseEntity.badRequest().body(new ErrorResponseDto("Incorrectly formed incoming request body."));
    }
}
