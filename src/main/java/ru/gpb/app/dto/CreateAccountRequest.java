package ru.gpb.app.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CreateAccountRequest(@NotNull Long userId,
                                   @NotBlank @Size(min = 5, max = 32) String userName,
                                   @NotBlank @Size(min = 3, max = 64) String accountName) {
}
