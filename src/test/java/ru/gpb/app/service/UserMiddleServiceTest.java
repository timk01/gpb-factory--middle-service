package ru.gpb.app.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateUserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMiddleServiceTest {

    @Mock
    private RestBackClient restBackClient;

    @InjectMocks
    private UserMiddleService middleService;

    private static CreateUserRequest properRequestId;
    private static CreateUserRequest improperRequestId;
    private static CreateUserRequest wrongRequestId;
    private static CreateAccountRequest properAccountRequest;
    private static String accountCreateUrl;
    private static String userCreateUrl;

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
    }

    @Test
    public void createUserWasOK() {
        when(restBackClient.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);

        UserCreationStatus result = restBackClient.createUser(properRequestId);

        assertThat(UserCreationStatus.USER_CREATED).isEqualTo(result);
        verify(restBackClient, times(1))
                .createUser(properRequestId);
    }

    @Test
    public void gettingUserByIdReturnedUser() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);

        UserRetrievalStatus result = restBackClient.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_FOUND).isEqualTo(result);
        verify(restBackClient, times(1))
                .getUserById(userId);
    }

    @Test
    public void createAccountWasOK() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_CREATED);

        AccountCreationStatus result = restBackClient.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_CREATED).isEqualTo(result);
        verify(restBackClient, times(1))
                .createAccount(properAccountRequest);
    }

    @Test
    public void gettingAccountsByIdReturnedAccount() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_FOUND);

        AccountRetrievalStatus result = restBackClient.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_FOUND).isEqualTo(result);
        verify(restBackClient, times(1))
                .getAccountsById(userId);
    }

    @Test
    public void createUserReturnedAlreadyExistedUser() {
        when(restBackClient.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);

        UserCreationStatus result = restBackClient.createUser(properRequestId);

        assertThat(UserCreationStatus.USER_ALREADY_EXISTS).isEqualTo(result);
        verify(restBackClient, times(1))
                .createUser(properRequestId);
    }

    @Test
    public void createAccountReturnedAlreadyExistedAccount() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS);

        AccountCreationStatus result = restBackClient.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS).isEqualTo(result);
        verify(restBackClient, times(1))
                .createAccount(properAccountRequest);
    }

    @Test
    public void gettingUserByIdDidNotReturnUser() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_NOT_FOUND);

        UserRetrievalStatus result = restBackClient.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_NOT_FOUND).isEqualTo(result);
        verify(restBackClient, times(1))
                .getUserById(userId);
    }

    @Test
    public void gettingAccountsByIdReturnedNoAccounts() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_NOT_FOUND);

        AccountRetrievalStatus result = restBackClient.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_NOT_FOUND).isEqualTo(result);
        verify(restBackClient, times(1))
                .getAccountsById(userId);
    }

    /**
     * Two following tests cover the common variant of returned Error. Actual returned error is tested in RestBackClientTest
     */
    @Test
    public void userCreateReturnedError() {
        when(restBackClient.createUser(improperRequestId)).thenReturn(UserCreationStatus.USER_ERROR);

        UserCreationStatus result = restBackClient.createUser(improperRequestId);

        assertThat(UserCreationStatus.USER_ERROR).isEqualTo(result);
        verify(restBackClient, times(1))
                .createUser(improperRequestId);
    }

    @Test
    public void accountCreateReturnedError() {
        when(restBackClient.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ERROR);

        AccountCreationStatus result = restBackClient.createAccount(properAccountRequest);

        assertThat(AccountCreationStatus.ACCOUNT_ERROR).isEqualTo(result);
        verify(restBackClient, times(1))
                .createAccount(properAccountRequest);
    }

    @Test
    public void gettingUserByIdReturnedError() {
        when(restBackClient.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_ERROR);

        UserRetrievalStatus result = restBackClient.getUserById(userId);

        assertThat(UserRetrievalStatus.USER_ERROR).isEqualTo(result);
        verify(restBackClient, times(1))
                .getUserById(userId);
    }

    @Test
    public void gettingAccountsByIdReturnedError() {
        when(restBackClient.getAccountsById(userId)).thenReturn(AccountRetrievalStatus.ACCOUNTS_ERROR);

        AccountRetrievalStatus result = restBackClient.getAccountsById(userId);

        assertThat(AccountRetrievalStatus.ACCOUNTS_ERROR).isEqualTo(result);
        verify(restBackClient, times(1))
                .getAccountsById(userId);
    }
}