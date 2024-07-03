package ru.gpb.app.service;

import org.springframework.http.ResponseEntity;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferResponse;

public interface TransferBackInterface {

    ResponseEntity<CreateTransferResponse> makeTransfer(CreateTransferRequest request);
}
