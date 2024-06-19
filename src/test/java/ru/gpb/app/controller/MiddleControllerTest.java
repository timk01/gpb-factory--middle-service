package ru.gpb.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.gpb.app.dto.CreateUserRequest;
import ru.gpb.app.service.UserCreationStatus;
import ru.gpb.app.service.UserMiddleService;

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

    @BeforeAll
    static void setUp() {
        properRequestId = new CreateUserRequest(868047670, "Khasmamedov");
        improperRequestId = new CreateUserRequest(1234567890, "Khasmamedov");
        wrongRequestId = new CreateUserRequest(-1234567890, "Khasmamedov");
    }

    @Test
    public void createUserSuccess() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_CREATED);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v2/api/users")
                        .content(asJsonString(properRequestId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(userMiddleService, times(1)).createUser(properRequestId);
    }


    @Test
    public void createUserWasWrongDueToAlreadyRegisteredUser() throws Exception {
        when(userMiddleService.createUser(properRequestId)).thenReturn(UserCreationStatus.USER_ALREADY_EXISTS);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v2/api/users")
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
    public void createUserNotCreatedDueToWrongData() throws Exception {
        when(userMiddleService.createUser(improperRequestId)).thenReturn(UserCreationStatus.USER_ERROR);

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v2/api/users")
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
    public void createUserNotCreatedDueToException() throws Exception {
        when(userMiddleService.createUser(wrongRequestId)).thenThrow(new RuntimeException("Some error"));

        mockMvc.perform(MockMvcRequestBuilders
                        .post("/v2/api/users")
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

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}