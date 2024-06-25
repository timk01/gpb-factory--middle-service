package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateUserRequest;

@Service
@Slf4j
public class UserMiddleService {

    private final RestBackClient restBackClient;

    public UserMiddleService(RestBackClient restBackClient) {
        this.restBackClient = restBackClient;
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
}