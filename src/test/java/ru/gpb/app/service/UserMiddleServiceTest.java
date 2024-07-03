package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferResponse;
import ru.gpb.app.dto.CreateUserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class UserMiddleServiceTest {

    @Mock
    private RestBackClient restBackClient;

    @Mock
    private TransferBackInterface transferBackInterface;

    @InjectMocks
    private UserMiddleService middleService;

    private static CreateUserRequest properRequestId;
    private static CreateUserRequest improperRequestId;
    private static CreateAccountRequest properAccountRequest;
    private static Long userId;

    private static CreateTransferRequest transferRequest;
    private static CreateTransferResponse transferResponse;

    @BeforeAll
    static void setUp() {
        userId = 868047670L;
        properRequestId = new CreateUserRequest(userId, "Khasmamedov");
        improperRequestId = new CreateUserRequest(1234567890L, "Khasmamedov");
        properAccountRequest = new CreateAccountRequest(
                userId,
                "Khasmamedov",
                "My first awesome account"
        );

        transferRequest = new CreateTransferRequest("Khasmamedov", "Durov", "100");
        transferResponse = new CreateTransferResponse("12345");
    }

    @Test
    public void createUserWasOK() {
        when(restBackClient.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);

        UserCreationStatus result = middleService.createUser(properRequestId);

        assertThat(UserCreationStatus.USER_CREATED).isEqualTo(result);
    }

    @Test
    public void gettingUserByIdReturnedUser() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);

        UserRetrievalStatus result = middleService.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_FOUND).isEqualTo(result);
    }

    @Test
    public void createAccountWasOK() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_CREATED);

        AccountCreationStatus result = middleService.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_CREATED).isEqualTo(result);
    }

    @Test
    public void gettingAccountsByIdReturnedAccount() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_FOUND);

        AccountRetrievalStatus result = middleService.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_FOUND).isEqualTo(result);
    }

    @Test
    public void createUserReturnedAlreadyExistedUser() {
        when(restBackClient.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);

        UserCreationStatus result = middleService.createUser(properRequestId);

        assertThat(UserCreationStatus.USER_ALREADY_EXISTS).isEqualTo(result);
    }

    @Test
    public void createAccountReturnedAlreadyExistedAccount() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS);

        AccountCreationStatus result = middleService.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS).isEqualTo(result);
    }

    @Test
    public void gettingUserByIdDidNotReturnUser() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_NOT_FOUND);

        UserRetrievalStatus result = middleService.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_NOT_FOUND).isEqualTo(result);
    }

    @Test
    public void gettingAccountsByIdReturnedNoAccounts() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_NOT_FOUND);

        AccountRetrievalStatus result = middleService.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_NOT_FOUND).isEqualTo(result);
    }

    @Test
    public void makeTransferReturnedOK() {
        when(transferBackInterface.makeTransfer(transferRequest)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        ResponseEntity<CreateTransferResponse> result = middleService.makeTransfer(transferRequest);

        assertThat(HttpStatus.OK).isEqualTo(result.getStatusCode());
    }

    @Test
    public void makeTransferReturnedWithClientError() {
        when(transferBackInterface.makeTransfer(transferRequest)).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        ResponseEntity<CreateTransferResponse> result = middleService.makeTransfer(transferRequest);

        assertThat(HttpStatus.BAD_REQUEST).isEqualTo(result.getStatusCode());
    }

    @Test
    public void makeTransferReturnedWithServerError() {
        when(transferBackInterface.makeTransfer(transferRequest)).thenReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<CreateTransferResponse> result = middleService.makeTransfer(transferRequest);

        assertThat(HttpStatus.INTERNAL_SERVER_ERROR).isEqualTo(result.getStatusCode());;
    }

    /**
     * Two following tests cover the common variant of returned Error. Actual returned error is tested in RestBackClientTest
     */
    @Test
    public void userCreateReturnedError() {
        when(restBackClient.createUser(improperRequestId)).thenReturn(UserCreationStatus.USER_ERROR);

        UserCreationStatus result = middleService.createUser(improperRequestId);

        assertThat(UserCreationStatus.USER_ERROR).isEqualTo(result);
    }

    @Test
    public void accountCreateReturnedError() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ERROR);

        AccountCreationStatus result = middleService.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_ERROR).isEqualTo(result);
    }

    @Test
    public void gettingUserByIdReturnedError() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_ERROR);

        UserRetrievalStatus result = middleService.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_ERROR).isEqualTo(result);
    }

    @Test
    public void gettingAccountsByIdReturnedError() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_ERROR);

        AccountRetrievalStatus result = middleService.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_ERROR).isEqualTo(result);
    }
}