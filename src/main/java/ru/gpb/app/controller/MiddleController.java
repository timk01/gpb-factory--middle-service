package ru.gpb.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.dto.Error;
import ru.gpb.app.service.UserCreationStatus;
import ru.gpb.app.service.UserMiddleService;

import javax.validation.Valid;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/v2/api")
public class MiddleController {

    private final UserMiddleService userMiddleService;

    @Autowired
    public MiddleController(UserMiddleService userMiddleService) {
        this.userMiddleService = userMiddleService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserRequest request) {
        ResponseEntity<?> responseEntity = null;

        try {
            UserCreationStatus userCreationStatus = userMiddleService.createUser(request);
            Error error;
            switch (userCreationStatus) {
                case USER_CREATED -> {
                    responseEntity = ResponseEntity.noContent().build();
                }
                case USER_ALREADY_EXISTS -> {
                    error = new Error(
                            "Пользователь уже зарегистрирован",
                            "CurrentUserIsAlreadyRegistered",
                            "409",
                            UUID.randomUUID()
                    );
                    responseEntity = ResponseEntity.status(HttpStatus.CONFLICT)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(error);
                }
                case USER_ERROR -> {
                    error = new Error(
                            "Ошибка при регистрации пользователя",
                            "UserCreationError",
                            "500",
                            UUID.randomUUID()
                    );
                    responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .contentType(MediaType.APPLICATION_JSON)
                            .body(error);
                }
            }
        } catch (Exception e) {
            Error error = new Error(
                    "Произошло что-то ужасное, но станет лучше, честно",
                    "GeneralError",
                    "123",
                    UUID.randomUUID()
            );
            responseEntity = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }

        return responseEntity;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    private ResponseEntity<Error> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Validation error: ", ex);
        Error error = new Error(
                "Ошибка валидации данных",
                "ValidationError",
                "400",
                UUID.randomUUID()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }
}