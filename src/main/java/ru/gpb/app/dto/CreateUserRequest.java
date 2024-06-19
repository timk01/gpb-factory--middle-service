package ru.gpb.app.dto;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public record CreateUserRequest(@NotNull long userId, @NotBlank @Size(min = 5, max = 32) String userName) {
}
