package ru.gpb.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gpb.app.dto.AccountListResponse;
import ru.gpb.app.dto.CreateAccountRequest;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MiddleController.class)
class MiddleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserMiddleService userMiddleService;

    private static CreateUserRequest properRequestId;
    private static CreateUserRequest improperRequestId;
    private static CreateUserRequest wrongRequestId;

    private static CreateAccountRequest properAccountRequest;
    private static CreateAccountRequest improperAccountRequest;
    private static String accountCreateUrl;
    private static String userCreateUrl;

    private static String getAccountsUrl;

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
        improperAccountRequest = new CreateAccountRequest(
                1234567890L,
                "Khasmamedov",
                "My first awesome account"
        );
        accountCreateUrl = "/v2/api/accounts";
        userCreateUrl = "/v2/api/users";
        getAccountsUrl = String.format("/v2/api/users/%d/accounts", userId);
    }

    @Test
    public void userWasCreated() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(properRequestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userMiddleService, times(1)).createUser(properRequestId);
    }

    @Test
    public void accountWasCreatedWithCreatedUser() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);
        when(userMiddleService.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_CREATED);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void accountWasCreatedWithAlreadyExistedUser() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);
        when(userMiddleService.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_CREATED);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void userWasNotCreatedDueToAlreadyRegisteredUser() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(properRequestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Пользователь уже зарегистрирован"))
                .andExpect(jsonPath("$.type").value("CurrentUserIsAlreadyRegistered"))
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
    }

    @Test
    public void accountWasNotCreatedDueToAlreadyCreatedAccount() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);
        when(userMiddleService.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ALREADY_EXISTS);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Такой счет у данного пользователя уже есть"))
                .andExpect(jsonPath("$.type").value("AccountAlreadyExists"))
                .andExpect(jsonPath("$.code").value("409"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void userWasNotCreatedDueToWrongData() throws Exception {
        when(userMiddleService.createUser(improperRequestId)).thenReturn(UserCreationStatus.USER_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(improperRequestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при регистрации пользователя"))
                .andExpect(jsonPath("$.type").value("UserCreationError"))
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(improperRequestId);
    }

    @Test
    public void accountWasNotCreatedDueToWrongUserData() throws Exception {
        when(userMiddleService.createUser(improperRequestId)).thenReturn(UserCreationStatus.USER_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(improperAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при регистрации пользователя"))
                .andExpect(jsonPath("$.type").value("UserCreationError"))
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(improperRequestId);
        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void accountWasNotCreatedDueToAccountError1() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);
        when(userMiddleService.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при создании счета"))
                .andExpect(jsonPath("$.type").value("AccountCreationError"))
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void accountWasNotCreatedDueToAccountError2() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);
        when(userMiddleService.createAccount(properAccountRequest)).thenReturn(AccountCreationStatus.ACCOUNT_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при создании счета"))
                .andExpect(jsonPath("$.type").value("AccountCreationError"))
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void userWasNotCreatedDueToException() throws Exception {
        when(userMiddleService.createUser(wrongRequestId)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(wrongRequestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Произошло что-то ужасное, но станет лучше, честно"))
                .andExpect(jsonPath("$.type").value("GeneralError"))
                .andExpect(jsonPath("$.code").value("123"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(wrongRequestId);
    }

    @Test
    public void accountWasNotCreatedDueToException1() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);
        when(userMiddleService.createAccount(properAccountRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Произошло что-то ужасное, но станет лучше, честно"))
                .andExpect(jsonPath("$.type").value("GeneralError"))
                .andExpect(jsonPath("$.code").value("123"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void accountWasNotCreatedDueToException2() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);
        when(userMiddleService.createAccount(properAccountRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(properAccountRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Произошло что-то ужасное, но станет лучше, честно"))
                .andExpect(jsonPath("$.type").value("GeneralError"))
                .andExpect(jsonPath("$.code").value("123"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).createUser(properRequestId);
        verify(userMiddleService, times(1)).createAccount(properAccountRequest);
    }

    @Test
    public void userWasNotCreatedDueToImproperId() throws Exception {
        CreateUserRequest nullIdRequest = new CreateUserRequest(null, "Khasmamedov");

        when(userMiddleService.createUser(nullIdRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(nullIdRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.type").value("ValidationError"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.traceId").exists());

        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void userWasNotCreatedDueToImproperName1() throws Exception {
        CreateUserRequest wrongUserNameRequest = new CreateUserRequest(868047670L, "123");

        when(userMiddleService.createUser(wrongUserNameRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(wrongUserNameRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.type").value("ValidationError"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.traceId").exists());

        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void userWasNotCreatedDueToImproperName2() throws Exception {
        CreateUserRequest nullUserNameRequest = new CreateUserRequest(868047670L, null);

        when(userMiddleService.createUser(nullUserNameRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(userCreateUrl)
                        .content(asJsonString(nullUserNameRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.type").value("ValidationError"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.traceId").exists());

        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void accountWasNotCreatedDueToImproperAccountName1() throws Exception {
        CreateAccountRequest nullAccountNameRequest = new CreateAccountRequest(
                868047670L,
                "Khasmamedov",
                null
        );

        when(userMiddleService.createAccount(nullAccountNameRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(nullAccountNameRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.type").value("ValidationError"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.traceId").exists());

        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void accountWasNotCreatedByIdDueToImproperAccountName2() throws Exception {
        CreateAccountRequest nullAccountNameRequest = new CreateAccountRequest(
                868047670L,
                "Khasmamedov",
                "AB"
        );

        when(userMiddleService.createAccount(nullAccountNameRequest)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post(accountCreateUrl)
                        .content(asJsonString(nullAccountNameRequest))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Ошибка валидации данных"))
                .andExpect(jsonPath("$.type").value("ValidationError"))
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.traceId").exists());

        verifyNoMoreInteractions(userMiddleService);
    }

    @Test
    public void accountWasNotRetrievedByIdDueToNotFoundUser() throws Exception {
        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_NOT_FOUND);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь не найден"))
                .andExpect(jsonPath("$.type").value("UserCannotBeFound"))
                .andExpect(jsonPath("$.code").value("404"));

        verify(userMiddleService, times(1)).getUserById(userId);
    }

    @Test
    public void accountWasNotRetrievedByIdDueToErrorWhilstGettingUser() throws Exception {
        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при получении пользователя"))
                .andExpect(jsonPath("$.type").value("UserRetrievingError"))
                .andExpect(jsonPath("$.code").value("500"));

        verify(userMiddleService, times(1)).getUserById(userId);
    }

    @Test
    public void accountWithDataWasRetrievedById() throws Exception {
        AccountListResponse[] accounts = new AccountListResponse[]{
                new AccountListResponse(
                        UUID.randomUUID(),
                        "Деньги на шашлык",
                        "203605.20"
                )
        };

        AccountRetrievalStatus accountsFound = AccountRetrievalStatus.ACCOUNTS_FOUND;
        accountsFound.setAccountListResponses(Arrays.asList(accounts));

        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);
        when(userMiddleService.getAccountsById(userId)).thenReturn(accountsFound);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].accountName").value("Деньги на шашлык"))
                .andExpect(jsonPath("$[0].amount").value("203605.20"));

        verify(userMiddleService, times(1)).getUserById(userId);
        verify(userMiddleService, times(1)).getAccountsById(userId);
    }

    @Test
    public void accountWithoutDataWasRetrievedById() throws Exception {
        AccountRetrievalStatus accountsNotFound = AccountRetrievalStatus.ACCOUNTS_NOT_FOUND;
        accountsNotFound.setAccountListResponses(Collections.emptyList());

        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);
        when(userMiddleService.getAccountsById(userId)).thenReturn(accountsNotFound);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userMiddleService, times(1)).getUserById(userId);
        verify(userMiddleService, times(1)).getAccountsById(userId);
    }

    @Test
    public void accountWasNotRetrievedByIdDueToError() throws Exception {
        AccountRetrievalStatus accountsError = AccountRetrievalStatus.ACCOUNTS_ERROR;

        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);
        when(userMiddleService.getAccountsById(userId)).thenReturn(accountsError);

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Ошибка при получении счетов"))
                .andExpect(jsonPath("$.type").value("AccountRetrievingError"))
                .andExpect(jsonPath("$.code").value("500"));

        verify(userMiddleService, times(1)).getUserById(userId);
        verify(userMiddleService, times(1)).getAccountsById(userId);
    }

    @Test
    public void accountWasNotRetrievedByIdDueToGeneralError() throws Exception {
        when(userMiddleService.getUserById(userId)).thenReturn(UserRetrievalStatus.USER_FOUND);
        when(userMiddleService.getAccountsById(userId)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .get(getAccountsUrl))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Произошло что-то ужасное, но станет лучше, честно"))
                .andExpect(jsonPath("$.type").value("GeneralError"))
                .andExpect(jsonPath("$.code").value("123"))
                .andExpect(jsonPath("$.traceId").exists());

        verify(userMiddleService, times(1)).getUserById(userId);
        verify(userMiddleService, times(1)).getAccountsById(userId);
    }


    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}