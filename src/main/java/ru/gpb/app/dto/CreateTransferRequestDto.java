package ru.gpb.app.dto;

import ru.gpb.app.controller.ValidId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CreateTransferRequestDto(@ValidId String from,
                                       @NotNull Long firstUserId,
                                       @ValidId String to,
                                       @NotBlank String amount) {
}
