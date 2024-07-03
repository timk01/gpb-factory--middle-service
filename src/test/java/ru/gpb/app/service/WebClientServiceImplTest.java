package ru.gpb.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;

import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WebClientServiceImplTest {
    @Mock
    private WebClientService webClientService;

    @InjectMocks
    private WebTransferBackClient webTransferBackClient;

    private CreateTransferRequest transferRequest;
    private CreateTransferResponse transferResponse;

    @BeforeEach
    public void setUp() {
        transferRequest = new CreateTransferRequest("Khasmamedov", "Durov", "100");
        transferResponse = new CreateTransferResponse("12345");
    }

    @Test
    public void makeTransferWasSuccessful() {
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenReturn(new ResponseEntity<>(transferResponse, HttpStatus.OK));

        ResponseEntity<CreateTransferResponse> result = webTransferBackClient.makeTransfer(transferRequest);

        assertThat("12345").isEqualTo(Objects.requireNonNull(result.getBody()).transferId());
    }

    @Test
    public void makeTransferGotWebClientResponseExceptionWithClientError() {
        WebClientResponseException exception = new WebClientResponseException("Client error", 400, "Bad request", null, null, null);
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenThrow(exception);

        assertThrows(WebClientResponseException.class, () -> {
            webClientService.makeAccountTransfer(transferRequest);
        });
    }

    @Test
    public void makeTransferGotWebClientResponseExceptionWithServerError() {
        WebClientResponseException exception = new WebClientResponseException("Server error", 500, "Bad request", null, null, null);
        when(webClientService.makeAccountTransfer(transferRequest))
                .thenThrow(exception);

        assertThrows(WebClientResponseException.class, () -> {
            webClientService.makeAccountTransfer(transferRequest);
        });
    }

    @Test
    public void makeTransferGotGeneralException() {
        Exception exception = new RuntimeException("Error");
        when(webClientService.makeAccountTransfer(transferRequest)).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> {
            webClientService.makeAccountTransfer(transferRequest);
        });
    }
}