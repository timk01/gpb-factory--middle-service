package ru.gpb.app.dto;

import java.util.UUID;

public record AccountListResponse(UUID accountId, String accountName, String amount) {
}
