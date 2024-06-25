package ru.gpb.app.dto;

import java.util.UUID;

public record CreateAccountResponse(UUID accountId, String accountName) {
}
