package com.emobile.springtodo.api.controller.handler;

import com.emobile.springtodo.api.output.ApiResponse;
import com.emobile.springtodo.api.output.error.ResponseError;
import com.emobile.springtodo.api.output.error.ResponseValidError;
import com.emobile.springtodo.api.output.error.ValidError;
import com.emobile.springtodo.core.exception.AccessRightsException;
import com.emobile.springtodo.core.exception.ObjectNotFoundException;
import com.emobile.springtodo.core.exception.UserAlreadyExistsException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.List;

@ControllerAdvice
@Slf4j
public class GlobalControllerHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<ResponseValidError> methodArgumentNotValidHandler(MethodArgumentNotValidException e) {
        log.warn("The request is not valid");
        List<ValidError> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map((f) -> new ValidError(f.getField(), f.getDefaultMessage()))
                .toList();
        return new ApiResponse<>(
                new ResponseValidError(HttpStatus.BAD_REQUEST.value(),
                        "Request is not valid",
                        Instant.now(),
                        errors), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessRightsException.class)
    public ApiResponse<ResponseError> methodAccessRightsException(AccessRightsException e) {
        log.error("An attempt to change the status of a task by a user who does not own the task");
        return getResponseError(HttpStatus.FORBIDDEN, e.getMessage());
    }

    @ExceptionHandler(ObjectNotFoundException.class)
    public ApiResponse<ResponseError> methodObjectNotFoundException(ObjectNotFoundException e) {
        log.error("Object not found in database");
        return getResponseError(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    public ApiResponse<ResponseError> methodUserAlreadyExistsException(UserAlreadyExistsException e) {
        log.error("A User Already Exists with such data");
        return getResponseError(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    private ApiResponse<ResponseError> getResponseError(HttpStatus status, String message) {
        return new ApiResponse<>(
                ResponseError.builder()
                        .status(status.value())
                        .message(message)
                        .timestamp(Instant.now())
                        .build(), status);
    }
}
