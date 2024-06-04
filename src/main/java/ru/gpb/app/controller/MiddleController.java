package ru.gpb.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.dto.Error;
import ru.gpb.app.dto.UserResponse;
import ru.gpb.app.service.UserMiddleService;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class MiddleController {

    private final UserMiddleService userMiddleService;

    @Autowired
    public MiddleController(UserMiddleService userMiddleService) {
        this.userMiddleService = userMiddleService;
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody CreateUserRequest request) {
        try {
            boolean userCreated = userMiddleService.createUser(request);
            if (userCreated) {
                return ResponseEntity.noContent().build();
            } else {
                Error error = new Error("Ошибка регистрации пользователя", "UserCreationError", "500", UUID.randomUUID());
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(error);
            }
        } catch (Exception e) {
            Error error = new Error("Произошло что-то ужасное, но станет лучше, честно", "GeneralError", "123", UUID.randomUUID());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(error);
        }
    }
}