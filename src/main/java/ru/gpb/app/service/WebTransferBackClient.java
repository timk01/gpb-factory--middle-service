package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferResponse;
import ru.gpb.app.dto.Error;

@Slf4j
@Service
public class WebTransferBackClient implements TransferBackInterface {

    private final WebClientService webClientService;

    public WebTransferBackClient(WebClientService webClientService) {
        this.webClientService = webClientService;
    }

    @Override
    public ResponseEntity<CreateTransferResponse> makeTransfer(CreateTransferRequest request) {
        try {
            log.info("Sending request to service C to make transfer");
            ResponseEntity<CreateTransferResponse> response = webClientService.makeAccountTransfer(request);
            log.info("Received response from service C: {}", response);
            return response;
        } catch (WebClientResponseException e) {
            log.error("WebClientResponseException happened during transfer attempt: ", e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Something serious happened in program while making transfer: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}

