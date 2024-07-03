package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.gpb.app.dto.CreateTransferRequest;
import ru.gpb.app.dto.CreateTransferRequestDto;
import ru.gpb.app.dto.CreateTransferResponse;
import org.springframework.http.HttpStatus;

@Slf4j
@Service
public class WebClientServiceImpl implements WebClientService {

    private final WebClient webClient;

    public WebClientServiceImpl(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public ResponseEntity<CreateTransferResponse> makeAccountTransfer(CreateTransferRequest request) {
        log.info("Using WebClient for transferring money");
        return webClient.post()
                .uri("/transfers")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, this::handle4xxError)
                .onStatus(HttpStatus::is5xxServerError, this::handle5xxError)
                .toEntity(CreateTransferResponse.class)
                .block();
    }

    private Mono<WebClientResponseException> handle4xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorMessage -> {
                    log.error("Client error in WebClient: {}", errorMessage);
                    return Mono.error(new WebClientResponseException(
                            errorMessage, clientResponse.statusCode().value(),
                            clientResponse.statusCode().getReasonPhrase(),
                            clientResponse.headers().asHttpHeaders(), null, null));
                });
    }

    private Mono<WebClientResponseException> handle5xxError(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(String.class)
                .flatMap(errorMessage -> {
                    log.error("Server error in WebClient: {}", errorMessage);
                    return Mono.error(new WebClientResponseException(
                            errorMessage, clientResponse.statusCode().value(),
                            clientResponse.statusCode().getReasonPhrase(),
                            clientResponse.headers().asHttpHeaders(), null, null));
                });
    }
}
