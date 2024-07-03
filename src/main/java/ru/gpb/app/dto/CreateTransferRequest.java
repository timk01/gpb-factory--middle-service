package ru.gpb.app.dto;

import ru.gpb.app.controller.ValidId;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public record CreateTransferRequest(String from,
                                    String to,
                                    String amount) {
}
