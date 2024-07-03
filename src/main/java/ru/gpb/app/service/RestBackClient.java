package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.*;

import java.util.Arrays;
import java.util.Collections;

@Slf4j
@Service
public class RestBackClient implements UserBackInterface, AccountBackInterface {

    private final RestTemplate restTemplate;

    public RestBackClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    private UserCreationStatus getUserCreationStatus(ResponseEntity<Void> response) {
        UserCreationStatus userCreationStatus;
        if (HttpStatus.NO_CONTENT == response.getStatusCode()) {
            log.info("Successfully send request to service C for user creation");
            userCreationStatus = UserCreationStatus.USER_CREATED;
        } else {
            log.error("Unexpected code-response while user registration: {}", response.getStatusCode());
            userCreationStatus = UserCreationStatus.USER_ERROR;
        }
        return userCreationStatus;
    }

    private UserCreationStatus getUserHttpClientErrorExceptionStatus(HttpClientErrorException e) {
        UserCreationStatus userCreationStatus;
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            log.warn("User already exists");
            userCreationStatus = UserCreationStatus.USER_ALREADY_EXISTS;
        } else {
            log.error("HttpClientErrorException happened in program while user creation: ", e);
            userCreationStatus = UserCreationStatus.USER_ERROR;
        }
        return userCreationStatus;
    }

    private UserCreationStatus getUserErrorStatus(Exception e) {
        log.error("Something serious happened in program while user creation: ", e);
        return UserCreationStatus.USER_ERROR;
    }

    @Override
    public UserCreationStatus createUser(CreateUserRequest request) {
        UserCreationStatus userCreationStatus;

        try {
            log.info("Sending request to service C to create user");
            ResponseEntity<Void> response = restTemplate.postForEntity("/users", request, Void.class);
            userCreationStatus = getUserCreationStatus(response);
        } catch (HttpClientErrorException e) {
            userCreationStatus = getUserHttpClientErrorExceptionStatus(e);
        } catch (Exception e) {
            userCreationStatus = getUserErrorStatus(e);
        }

        return userCreationStatus;
    }

    private AccountCreationStatus getAccountCreationStatus(ResponseEntity<Void> response) {
        AccountCreationStatus accountCreationStatus;
        if (HttpStatus.NO_CONTENT == response.getStatusCode()) {
            log.info("Successfully send request to service C for account creation");
            accountCreationStatus = AccountCreationStatus.ACCOUNT_CREATED;
        } else {
            log.error("Unexpected code-response while account creation: {}", response.getStatusCode());
            accountCreationStatus = AccountCreationStatus.ACCOUNT_ERROR;
        }
        return accountCreationStatus;
    }

    private AccountCreationStatus getAccountHttpClientErrorExceptionStatus(HttpClientErrorException e) {
        AccountCreationStatus accountCreationStatus;
        if (e.getStatusCode() == HttpStatus.CONFLICT) {
            log.warn("Account already exists");
            accountCreationStatus = AccountCreationStatus.ACCOUNT_ALREADY_EXISTS;
        } else {
            log.error("HttpStatusCodeException happened in program while account creation: ", e);
            accountCreationStatus = AccountCreationStatus.ACCOUNT_ERROR;
        }
        return accountCreationStatus;
    }

    private AccountCreationStatus getAccountErrorStatus(Exception e) {
        log.error("Something serious happened in program while user creation: ", e);
        return AccountCreationStatus.ACCOUNT_ERROR;
    }

    @Override
    public AccountCreationStatus createAccount(CreateAccountRequest request) {
        AccountCreationStatus accountCreationStatus;

        try {
            log.info("Sending request to service C to create account");
            String url = String.format("/users/%d/accounts", request.userId());
            ResponseEntity<Void> response = restTemplate.postForEntity(url, request, Void.class);
            accountCreationStatus = getAccountCreationStatus(response);
        } catch (HttpClientErrorException e) {
            accountCreationStatus = getAccountHttpClientErrorExceptionStatus(e);
        } catch (Exception e) {
            accountCreationStatus = getAccountErrorStatus(e);
        }

        return accountCreationStatus;
    }

    @Override
    public UserRetrievalStatus getUserById(Long userId) {
        UserRetrievalStatus userRetrievalStatus;

        try {
            String url = String.format("/users/%d", userId);
            ResponseEntity<Void> response = restTemplate.getForEntity(url, Void.class);
            log.info("Sending request to service C to check user");
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Successfully send request to service C, user with {} is found", userId);
                userRetrievalStatus = UserRetrievalStatus.USER_FOUND;
            } else {
                log.info("Successfully send request to service C, user with {} is not found", userId);
                userRetrievalStatus = UserRetrievalStatus.USER_NOT_FOUND;
            }
        } catch (RestClientException e) {
            log.error("RestClientException happened in program while checking if user is registered: ", e);
            userRetrievalStatus = UserRetrievalStatus.USER_ERROR;
        } catch (Exception e) {
            log.error("Something serious happened in program while checking if user is registered: ", e);
            userRetrievalStatus = UserRetrievalStatus.USER_ERROR;
        }
        return userRetrievalStatus;
    }

    @Override
    public AccountRetrievalStatus getAccountsById(Long userId) {
        log.info("Sending request to service C to get accounts for user {}", userId);

        AccountRetrievalStatus accountRetrievalStatus = null;
        try {
            String url = String.format("/users/%d/accounts", userId);
            ResponseEntity<AccountListResponse[]> accountsEntity = restTemplate.getForEntity(url, AccountListResponse[].class);
            if (accountsEntity.getStatusCode() == HttpStatus.OK) {
                AccountListResponse[] accounts = accountsEntity.getBody();
                if (accounts != null && accounts.length > 0) {
                    log.info("Successfully send request to service C, accounts for user with {} are found", userId);
                    accountRetrievalStatus = AccountRetrievalStatus.ACCOUNTS_FOUND;
                    accountRetrievalStatus.setAccountListResponses(Arrays.asList(accounts));
                } else {
                    log.info("Successfully send request to service C, NO accounts for user with {} are found", userId);
                    accountRetrievalStatus = AccountRetrievalStatus.ACCOUNTS_NOT_FOUND;
                    accountRetrievalStatus.setAccountListResponses(Collections.emptyList());
                }
            }
        } catch (HttpStatusCodeException e) {
            log.error("HttpStatusCodeException happened in program while retreiving accounts: ", e);
            accountRetrievalStatus = AccountRetrievalStatus.ACCOUNTS_ERROR;
        } catch (Exception e) {
            log.error("Something serious happened in program while retreiving accounts: ", e);
            accountRetrievalStatus = AccountRetrievalStatus.ACCOUNTS_ERROR;
        }
        return accountRetrievalStatus;
    }
}
