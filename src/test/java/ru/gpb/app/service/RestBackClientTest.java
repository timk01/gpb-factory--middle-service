package ru.gpb.app.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateUserRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestBackClientTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private RestBackClient restBackClientService;

    private static CreateUserRequest properRequestId;
    private static CreateUserRequest improperRequestId;
    private static CreateUserRequest wrongRequestId;
    private static CreateAccountRequest properAccountRequest;
    private static String accountCreateUrl;
    private static String userCreateUrl;
    private static String gettingUserUrl;

    private static String gettingAccountsUrl;

    private static Long userId;

    @BeforeAll
    static void setUp() {
        userId = 868047670L;
        properRequestId = new CreateUserRequest(userId, "Khasmamedov");
        improperRequestId = new CreateUserRequest(1234567890L, "Khasmamedov");
        wrongRequestId = new CreateUserRequest(-1234567890L, "Khasmamedov");
        properAccountRequest = new CreateAccountRequest(
                userId,
                "Khasmamedov",
                "My first awesome account"
        );
        accountCreateUrl = String.format("/users/%d/accounts", userId);
        userCreateUrl = "/users";
        gettingUserUrl = String.format("/users/%d", userId);
        gettingAccountsUrl = String.format("/users/%d/accounts", userId);
    }

    @Test
    public void createUserReturned204() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.postForEntity(userCreateUrl, properRequestId, Void.class))
                .thenReturn(response);

        UserCreationStatus result = restBackClientService.createUser(properRequestId);

        assertThat(result).isEqualTo(UserCreationStatus.USER_CREATED);
        verify(restTemplate, times(1))
                .postForEntity(userCreateUrl, properRequestId, Void.class);
    }

    @Test
    public void createAccountReturned204() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.postForEntity(accountCreateUrl, properAccountRequest, Void.class))
                .thenReturn(response);

        AccountCreationStatus result = restBackClientService.createAccount(properAccountRequest);

        assertThat(result).isEqualTo(AccountCreationStatus.ACCOUNT_CREATED);
        verify(restTemplate, times(1))
                .postForEntity(accountCreateUrl, properAccountRequest, Void.class);
    }

    @Test
    public void createUserReturned409() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.CONFLICT);
        when(restTemplate.postForEntity(userCreateUrl, properRequestId, Void.class))
                .thenReturn(response);

        UserCreationStatus result = restBackClientService.createUser(properRequestId);

        assertThat(result).isEqualTo(UserCreationStatus.USER_ALREADY_EXISTS);
        verify(restTemplate, times(1))
                .postForEntity(userCreateUrl, properRequestId, Void.class);
    }

    @Test
    public void createAccountReturned409() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.CONFLICT);
        when(restTemplate.postForEntity(accountCreateUrl, properAccountRequest, Void.class))
                .thenReturn(response);

        AccountCreationStatus result = restBackClientService.createAccount(properAccountRequest);

        assertThat(result).isEqualTo(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS);
        verify(restTemplate, times(1))
                .postForEntity(accountCreateUrl, properAccountRequest, Void.class);
    }

    @Test
    public void userCreateReturnedNot204Or409() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> mockedResponse = mock(ResponseEntity.class);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(userCreateUrl, improperRequestId, Void.class))
                .thenReturn(mockedResponse);

        UserCreationStatus result = restBackClientService.createUser(improperRequestId);

        assertThat(result).isEqualTo(UserCreationStatus.USER_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(userCreateUrl, improperRequestId, Void.class);
    }

    @Test
    public void accountCreateReturnedNot204Or409() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> mockedResponse = mock(ResponseEntity.class);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity(accountCreateUrl, properAccountRequest, Void.class))
                .thenReturn(mockedResponse);

        AccountCreationStatus result = restBackClientService.createAccount(properAccountRequest);

        assertThat(result).isEqualTo(AccountCreationStatus.ACCOUNT_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(accountCreateUrl, properAccountRequest, Void.class);
    }

    @Test
    public void createUserResultedInHttpStatusCodeException() {
        HttpStatusCodeException httpStatusCodeException = new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        when(restTemplate.postForEntity(userCreateUrl, wrongRequestId, Void.class))
                .thenThrow(httpStatusCodeException);

        UserCreationStatus result = restBackClientService.createUser(wrongRequestId);

        assertThat(result).isEqualTo(UserCreationStatus.USER_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(userCreateUrl, wrongRequestId, Void.class);
    }

    @Test
    public void createAccountResultedInHttpStatusCodeException() {
        HttpStatusCodeException httpStatusCodeException = new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        when(restTemplate.postForEntity(accountCreateUrl, properAccountRequest, Void.class))
                .thenThrow(httpStatusCodeException);

        AccountCreationStatus result = restBackClientService.createAccount(properAccountRequest);

        assertThat(result).isEqualTo(AccountCreationStatus.ACCOUNT_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(accountCreateUrl, properAccountRequest, Void.class);
    }

    @Test
    public void createUserQuerySuccessfullySendAndReturnedGeneralException() {
        RuntimeException seriousException = new RuntimeException("Serious exception") {
        };
        when(restTemplate.postForEntity(userCreateUrl, wrongRequestId, Void.class))
                .thenThrow(seriousException);

        UserCreationStatus result = restBackClientService.createUser(wrongRequestId);

        assertThat(result).isEqualTo(UserCreationStatus.USER_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(userCreateUrl, wrongRequestId, Void.class);
    }

    @Test
    public void createAccountQuerySuccessfullySendAndReturnedGeneralException() {
        RuntimeException seriousException = new RuntimeException("Serious exception") {
        };
        when(restTemplate.postForEntity(accountCreateUrl, properAccountRequest, Void.class))
                .thenThrow(seriousException);

        AccountCreationStatus result = restBackClientService.createAccount(properAccountRequest);

        assertThat(result).isEqualTo(AccountCreationStatus.ACCOUNT_ERROR);
        verify(restTemplate, times(1))
                .postForEntity(accountCreateUrl, properAccountRequest, Void.class);
    }

    /**
     *
     * group tests for retreiving user//account
     */

    @Test
    public void gettingUserByIdWasSuccessful() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.OK);
        when(restTemplate.getForEntity(gettingUserUrl, Void.class))
                .thenReturn(response);

        UserRetrievalStatus result = restBackClientService.getUserById(userId);

        assertThat(result).isEqualTo(UserRetrievalStatus.USER_FOUND);
        verify(restTemplate, times(1))
                .getForEntity(gettingUserUrl, Void.class);
    }

    @Test
    public void gettingUserByIdWasUnsuccessful() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> mockedResponse = mock(ResponseEntity.class);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.getForEntity(gettingUserUrl, Void.class))
                .thenReturn(mockedResponse);

        UserRetrievalStatus result = restBackClientService.getUserById(userId);

        assertThat(result).isEqualTo(UserRetrievalStatus.USER_NOT_FOUND);
        verify(restTemplate, times(1))
                .getForEntity(gettingUserUrl, Void.class);
    }

    @Test
    public void gettingUserByIdFacedHttpStatusCodeException() {
        RestClientException restClientException = new RestClientException("Internal Server Error");

        when(restTemplate.getForEntity(gettingUserUrl, Void.class))
                .thenThrow(restClientException);

        UserRetrievalStatus result = restBackClientService.getUserById(userId);

        assertThat(result).isEqualTo(UserRetrievalStatus.USER_ERROR);
        verify(restTemplate, times(1))
                .getForEntity(gettingUserUrl, Void.class);
    }

    @Test
    public void gettingUserByIdFacedGeneralException() {
        RuntimeException seriousException = new RuntimeException("Serious exception") {
        };
        when(restTemplate.getForEntity(gettingUserUrl, Void.class)).thenThrow(seriousException);

        UserRetrievalStatus result = restBackClientService.getUserById(userId);

        assertThat(result).isEqualTo(UserRetrievalStatus.USER_ERROR);
        verify(restTemplate, times(1))
                .getForEntity(gettingUserUrl, Void.class);
    }

    @Test
    public void gettingAccountsByIdWasSuccessful() {
        AccountListResponse[] accounts = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };
        ResponseEntity<AccountListResponse[]> response = new ResponseEntity<>(accounts, HttpStatus.OK);
        when(restTemplate.getForEntity(gettingAccountsUrl, AccountListResponse[].class))
                .thenReturn(response);

        AccountRetrievalStatus result = restBackClientService.getAccountsById(userId);

        assertThat(result.getAccountListResponses())
                .isEqualTo(Arrays.asList(accounts));
        verify(restTemplate, times(1))
                .getForEntity(gettingAccountsUrl, AccountListResponse[].class);
    }

    @Test
    public void gettingAccountsByIdWasReturnedWithNoAccounts() {
        AccountListResponse[] accounts = new AccountListResponse[]{};
        ResponseEntity<AccountListResponse[]> response = new ResponseEntity<>(accounts, HttpStatus.OK);
        when(restTemplate.getForEntity(gettingAccountsUrl, AccountListResponse[].class))
                .thenReturn(response);

        AccountRetrievalStatus result = restBackClientService.getAccountsById(userId);

        assertThat(result)
                .isEqualTo(AccountRetrievalStatus.ACCOUNTS_NOT_FOUND)
                .extracting(AccountRetrievalStatus::getAccountListResponses)
                .isEqualTo(Collections.emptyList());
        verify(restTemplate, times(1))
                .getForEntity(gettingAccountsUrl, AccountListResponse[].class);
    }

    @Test
    public void gettingAccountsByIdFacedGeneralException() {
        HttpStatusCodeException httpStatusCodeException = new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        when(restTemplate.getForEntity(gettingAccountsUrl, AccountListResponse[].class))
                .thenThrow(httpStatusCodeException);

        AccountRetrievalStatus result = restBackClientService.getAccountsById(userId);

        assertThat(result).isEqualTo(AccountRetrievalStatus.ACCOUNTS_ERROR);
        verify(restTemplate, times(1))
                .getForEntity(gettingAccountsUrl, AccountListResponse[].class);
    }

    @Test
    public void gettingAccountsByIdFacedHttpStatusCodeException() {
        RuntimeException seriousException = new RuntimeException("Serious exception") {
        };
        when(restTemplate.getForEntity(gettingAccountsUrl, AccountListResponse[].class))
                .thenThrow(seriousException);

        AccountRetrievalStatus result = restBackClientService.getAccountsById(userId);

        assertThat(result).isEqualTo(AccountRetrievalStatus.ACCOUNTS_ERROR);
        verify(restTemplate, times(1))
                .getForEntity(gettingAccountsUrl, AccountListResponse[].class);
    }
}