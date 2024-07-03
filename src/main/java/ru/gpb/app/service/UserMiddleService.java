package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferResponse;
import ru.gpb.app.dto.CreateUserRequest;

@Service
@Slf4j
public class UserMiddleService {

    private final RestBackClient restBackClient;
    private final TransferBackInterface transferBackInterface;

    public UserMiddleService(RestBackClient restBackClient, TransferBackInterface transferBackInterface) {
        this.restBackClient = restBackClient;
        this.transferBackInterface = transferBackInterface;
    }

    public UserCreationStatus createUser(CreateUserRequest request) {
        return restBackClient.createUser(request);
    }

    public AccountCreationStatus createAccount(CreateAccountRequest request) {
        return restBackClient.createAccount(request);
    }

    public UserRetrievalStatus getUserById(Long userId) {
        return restBackClient.getUserById(userId);
    }

    public AccountRetrievalStatus getAccountsById(Long userId) {
        return restBackClient.getAccountsById(userId);
    }

    public ResponseEntity<CreateTransferResponse> makeTransfer(CreateTransferRequest request) {
        return transferBackInterface.makeTransfer(request);
    }
}