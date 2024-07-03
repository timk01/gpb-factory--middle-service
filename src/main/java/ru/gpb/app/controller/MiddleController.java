package ru.gpb.app.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.gpb.app.dto.*;
import ru.gpb.app.dto.Error;
import ru.gpb.app.mapper.TransferRequestConverter;
import ru.gpb.app.service.*;
import ru.gpb.app.dto.Error;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Validated
@RestController
@RequestMapping("/v2/api")
public class MiddleController {

    private final UserMiddleService userMiddleService;
    private final GlobalExceptionHandler globalExceptionHandler;

    private final TransferRequestConverter converter;

    @Autowired
    public MiddleController(UserMiddleService userMiddleService, GlobalExceptionHandler globalExceptionHandler, TransferRequestConverter converter) {
        this.userMiddleService = userMiddleService;
        this.globalExceptionHandler = globalExceptionHandler;
        this.converter = converter;
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

    @PostMapping("/users/{userId}/accounts")
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

        AccountRetrievalStatus accountsById = userMiddleService.getAccountsById(userId);
        if (accountsById == AccountRetrievalStatus.ACCOUNTS_NOT_FOUND) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("Нет счетов у пользователя");
        }
        return handlerForRetrievingAccounts(accountsById);
    }

    private Optional<ResponseEntity<Error>> getErrorResponseEntity(ResponseEntity<?> firstUserAccounts) {
        ResponseEntity<Error> error = null;
        if (HttpStatus.NO_CONTENT == firstUserAccounts.getStatusCode()) {
            return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new Error(
                            "Аккаунт первого пользователя не найден",
                            "AccountUserNumberOneNotFoundError",
                            "404",
                            UUID.randomUUID()
                    )
            ));
        }
        return Optional.empty();
    }

    private Optional<ResponseEntity<Error>> checkAccountFunds(CreateTransferRequestDto request, List<AccountListResponse> accountData) {
        if (accountData != null && !accountData.isEmpty()) {
            BigDecimal accountMoney = BigDecimal.valueOf(Double.parseDouble(accountData.get(0).amount()));
            BigDecimal transferMoney = BigDecimal.valueOf(Double.parseDouble(request.amount()));
            if (accountMoney.compareTo(transferMoney) < 0) {
                return Optional.of(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        new Error("Недостаточно средств на счету",
                                "InsufficientFundsError",
                                "400",
                                UUID.randomUUID())
                ));
            }
        }
        return Optional.empty();
    }

    private ResponseEntity<Error> problemsChecker(CreateTransferRequestDto request) {
        log.info("Received transfer request: {}", request);

        ResponseEntity<?> firstUserAccounts = getAccount(request.firstUserId());
        log.info("First user accounts response: {}", firstUserAccounts);

        Optional<ResponseEntity<Error>> possibleErrorForFirstUser = getErrorResponseEntity(firstUserAccounts);
        if (possibleErrorForFirstUser.isPresent()) {
            log.info("Error in first user accounts: {}", possibleErrorForFirstUser.get());
            return possibleErrorForFirstUser.get();
        }

        if (firstUserAccounts.getBody() instanceof Error) {
            return (ResponseEntity<Error>) firstUserAccounts;
        }

        List<AccountListResponse> accountData = (List<AccountListResponse>) firstUserAccounts.getBody();
        log.info("Account data for first user: {}", accountData);

        Optional<ResponseEntity<Error>> fundsProblem = checkAccountFunds(request, accountData);
        if (fundsProblem.isPresent()) {
            log.info("Funds problem: {}", fundsProblem.get());
            return fundsProblem.get();
        }
        return null;
    }

    @PostMapping("/transfers")
    public ResponseEntity<?> makeTransfer(@Valid @RequestBody CreateTransferRequestDto request) {
        ResponseEntity<Error> possibleErrorForFirstUser = problemsChecker(request);
        if (possibleErrorForFirstUser != null) {
            return possibleErrorForFirstUser;
        }

        ResponseEntity<CreateTransferResponse> transferResponse = userMiddleService.makeTransfer(converter.convertToCreateTransferRequest(request));
        log.info("Transfer response: {}", transferResponse);
        return transferResponse;
    }
}