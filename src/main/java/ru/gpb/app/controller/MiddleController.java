package ru.gpb.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.dto.Error;
import ru.gpb.app.service.*;

import javax.validation.Valid;
import java.util.Optional;

@Slf4j
@Validated
@RestController
@RequestMapping("/v2/api")
public class MiddleController {

    private final UserMiddleService userMiddleService;
    private final GlobalExceptionHandler globalExceptionHandler;

    @Autowired
    public MiddleController(UserMiddleService userMiddleService, GlobalExceptionHandler globalExceptionHandler) {
        this.userMiddleService = userMiddleService;
        this.globalExceptionHandler = globalExceptionHandler;
    }

    private ResponseEntity<?> handlerForUserCreation(UserCreationStatus userCreationStatus) {
        return switch (userCreationStatus) {
            case USER_CREATED -> ResponseEntity.noContent().build();
            case USER_ALREADY_EXISTS -> globalExceptionHandler.errorResponseEntityBuilder(
                    "Пользователь уже зарегистрирован",
                    "CurrentUserIsAlreadyRegistered",
                    "409",
                    HttpStatus.CONFLICT
            );
            case USER_ERROR -> globalExceptionHandler.errorResponseEntityBuilder(
                    "Ошибка при регистрации пользователя",
                    "UserCreationError",
                    "500",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        };
    }

    @PostMapping("/users")
    public ResponseEntity<?> createUser(@RequestBody @Valid CreateUserRequest request) {
        return handlerForUserCreation(userMiddleService.createUser(request));
    }

    private ResponseEntity<?> handlerForAccountCreation(AccountCreationStatus accountCreationStatus) {
        return switch (accountCreationStatus) {
            case ACCOUNT_CREATED -> ResponseEntity.noContent().build();
            case ACCOUNT_ALREADY_EXISTS -> globalExceptionHandler.errorResponseEntityBuilder(
                    "Такой счет у данного пользователя уже есть",
                    "AccountAlreadyExists",
                    "409",
                    HttpStatus.CONFLICT
            );
            case ACCOUNT_ERROR -> globalExceptionHandler.errorResponseEntityBuilder(
                    "Ошибка при создании счета",
                    "AccountCreationError",
                    "500",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        };
    }

    @PostMapping("/accounts")
    public ResponseEntity<?> createAccount(@RequestBody @Valid CreateAccountRequest request) {
        UserCreationStatus userCreationStatus = userMiddleService.
                createUser(new CreateUserRequest(request.userId(), request.userName()));
        if (userCreationStatus != UserCreationStatus.USER_CREATED &&
                userCreationStatus != UserCreationStatus.USER_ALREADY_EXISTS) {
            return globalExceptionHandler.errorResponseEntityBuilder(
                    "Ошибка при регистрации пользователя",
                    "UserCreationError",
                    "500",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }

        AccountCreationStatus accountCreationStatus = userMiddleService.createAccount(request);
        return handlerForAccountCreation(accountCreationStatus);
    }

    private Optional<ResponseEntity<Error>> checkUserRetrievalStatus(UserRetrievalStatus userRetrievalStatus) {
        return switch (userRetrievalStatus) {
            case USER_NOT_FOUND -> Optional.of(globalExceptionHandler.errorResponseEntityBuilder(
                            "Пользователь не найден",
                            "UserCannotBeFound",
                            "404",
                            HttpStatus.NOT_FOUND
                    )
            );
            case USER_ERROR -> Optional.of(globalExceptionHandler.errorResponseEntityBuilder(
                            "Ошибка при получении пользователя",
                            "UserRetrievingError",
                            "500",
                            HttpStatus.INTERNAL_SERVER_ERROR
                    )
            );
            default -> Optional.empty();
        };
    }

    private ResponseEntity<?> handlerForRetrievingAccounts(AccountRetrievalStatus accountRetrievalStatus) {
        return switch (accountRetrievalStatus) {
            case ACCOUNTS_FOUND -> ResponseEntity.ok(accountRetrievalStatus.getAccountListResponses());
            case ACCOUNTS_NOT_FOUND -> ResponseEntity.noContent().build();
            case ACCOUNTS_ERROR -> globalExceptionHandler.errorResponseEntityBuilder(
                    "Ошибка при получении счетов",
                    "AccountRetrievingError",
                    "500",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        };
    }

    @GetMapping("users/{userId}/accounts")
    public ResponseEntity<?> getAccount(@PathVariable Long userId) {
        Optional<ResponseEntity<Error>> retreivalStatus = checkUserRetrievalStatus(userMiddleService.getUserById(userId));
        if (retreivalStatus.isPresent()) {
            return retreivalStatus.get();
        }

        return handlerForRetrievingAccounts(userMiddleService.getAccountsById(userId));
    }
}