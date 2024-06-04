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
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserMiddleServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private UserMiddleService middleService;

    private static CreateUserRequest properRequestId;
    private static CreateUserRequest improperRequestId;
    private static CreateUserRequest wrongRequestId;

    @BeforeAll
    static void setUp() {
        properRequestId = new CreateUserRequest(868047670);
        improperRequestId = new CreateUserRequest(1234567890);
        wrongRequestId = new CreateUserRequest(-1234567890);
    }

    @Test
    public void createUserQuerySuccessfullySendAndReturned204() {
        ResponseEntity<Void> response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
        when(restTemplate.postForEntity("/users", properRequestId, Void.class))
                .thenReturn(response);

        boolean res = middleService.createUser(properRequestId);

        assertThat(res).isTrue();
        verify(restTemplate, times(1))
                .postForEntity("/users", properRequestId, Void.class);
    }


    @Test
    public void createUserQuerySuccessfullySendAndReturnedNot204() {
        @SuppressWarnings("unchecked")
        ResponseEntity<Void> mockedResponse = mock(ResponseEntity.class);
        when(mockedResponse.getStatusCode()).thenReturn(HttpStatus.BAD_REQUEST);
        when(restTemplate.postForEntity("/users", improperRequestId, Void.class))
                .thenReturn(mockedResponse);

        boolean res = middleService.createUser(improperRequestId);

        assertThat(res).isFalse();
        verify(restTemplate, times(1))
                .postForEntity("/users", improperRequestId, Void.class);
    }


    @Test
    public void createUserQuerySuccessfullySendAndReturnedHttpStatusCodeException() {
        HttpStatusCodeException httpStatusCodeException = new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR) {
        };
        when(restTemplate.postForEntity("/users", wrongRequestId, Void.class))
                .thenThrow(httpStatusCodeException);

        boolean res = middleService.createUser(wrongRequestId);

        assertThat(res).isFalse();
        verify(restTemplate, times(1))
                .postForEntity("/users", wrongRequestId, Void.class);
    }

    @Test
    public void createUserQuerySuccessfullySendAndReturnedGeneralException() {
        RuntimeException seriousException = new RuntimeException("Serious exception") {
        };
        when(restTemplate.postForEntity("/users", wrongRequestId, Void.class))
                .thenThrow(seriousException);

        boolean res = middleService.createUser(wrongRequestId);

        assertThat(res).isFalse();
        verify(restTemplate, times(1))
                .postForEntity("/users", wrongRequestId, Void.class);
    }
}