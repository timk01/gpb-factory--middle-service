package ru.gpb.app.mapper;

import org.springframework.stereotype.Component;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;

@Component
public class TransferRequestConverter {
    public CreateTransferRequest convertToCreateTransferRequest(CreateTransferRequestDto dto) {
        return new CreateTransferRequest(dto.from(), dto.to(), dto.amount());
    }
}