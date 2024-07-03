package ru.gpb.app.service;

import org.springframework.http.ResponseEntity;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;

public interface WebClientService {
    ResponseEntity<CreateTransferResponse> makeAccountTransfer(CreateTransferRequest request);
}