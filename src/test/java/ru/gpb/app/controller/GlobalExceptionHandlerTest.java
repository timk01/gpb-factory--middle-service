package ru.gpb.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.gpb.app.dto.Error;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    public void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void errorResponseEntityBuilder() {
        Error error = new Error(
                "Такой счет у данного пользователя уже есть",
                "AccountAlreadyExists",
                "409",
                UUID.randomUUID()
        );
        HttpStatus status = HttpStatus.CONFLICT;

        ResponseEntity<Error> expectedEntity = ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);

        ResponseEntity<Error> actualEntity = globalExceptionHandler.errorResponseEntityBuilder(error.message(),
                error.type(),
                error.code(),
                status);

        assertAll("multiple assertions for errorResponseEntityBuilder",
                () -> assertThat(expectedEntity.getStatusCode()).isEqualTo(actualEntity.getStatusCode()),
                () -> assertThat(expectedEntity.getHeaders().getContentType()).isEqualTo(actualEntity.getHeaders().getContentType()),
                () -> assertThat(actualEntity.getBody()).isNotNull(),
                () -> assertThat(expectedEntity.getBody().message()).isEqualTo(actualEntity.getBody().message()),
                () -> assertThat(expectedEntity.getBody().type()).isEqualTo(actualEntity.getBody().type()),
                () -> assertThat(expectedEntity.getBody().code()).isEqualTo(actualEntity.getBody().code())
        );
    }

    @Test
    void handleOfValidationException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);

        Error error = new Error(
                "Ошибка валидации данных",
                "ValidationError",
                "400",
                UUID.randomUUID()
        );
        HttpStatus status = HttpStatus.BAD_REQUEST;

        ResponseEntity<Error> expectedEntity = ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);

        ResponseEntity<Error> actualEntity = globalExceptionHandler.handleOfValidationException(ex);

        assertAll("multiple assertions for handleOfValidationException",
                () -> assertThat(expectedEntity.getStatusCode()).isEqualTo(actualEntity.getStatusCode()),
                () -> assertThat(expectedEntity.getHeaders().getContentType()).isEqualTo(actualEntity.getHeaders().getContentType()),
                () -> assertThat(actualEntity.getBody()).isNotNull(),
                () -> assertThat(expectedEntity.getBody().message()).isEqualTo(actualEntity.getBody().message()),
                () -> assertThat(expectedEntity.getBody().type()).isEqualTo(actualEntity.getBody().type()),
                () -> assertThat(expectedEntity.getBody().code()).isEqualTo(actualEntity.getBody().code())
        );
    }

    @Test
    void handleGeneralException() {
        Exception ex = new Exception("General Exception");

        Error error = new Error(
                "Произошло что-то ужасное, но станет лучше, честно",
                "GeneralError",
                "123",
                UUID.randomUUID()
        );
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

        ResponseEntity<Error> expectedEntity = ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);

        ResponseEntity<Error> actualEntity = globalExceptionHandler.handleGeneralException(ex);

        assertAll("multiple assertions for handleGeneralException",
                () -> assertThat(expectedEntity.getStatusCode()).isEqualTo(actualEntity.getStatusCode()),
                () -> assertThat(expectedEntity.getHeaders().getContentType()).isEqualTo(actualEntity.getHeaders().getContentType()),
                () -> assertThat(actualEntity.getBody()).isNotNull(),
                () -> assertThat(expectedEntity.getBody().message()).isEqualTo(actualEntity.getBody().message()),
                () -> assertThat(expectedEntity.getBody().type()).isEqualTo(actualEntity.getBody().type()),
                () -> assertThat(expectedEntity.getBody().code()).isEqualTo(actualEntity.getBody().code())
        );
    }
}