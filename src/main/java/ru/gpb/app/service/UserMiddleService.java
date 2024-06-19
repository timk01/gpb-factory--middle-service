package ru.gpb.app.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.gpb.app.dto.CreateUserRequest;

@Service
@Slf4j
public class UserMiddleService {

    private final RestTemplate restTemplate;

    @Autowired
    public UserMiddleService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public UserCreationStatus createUser(CreateUserRequest request) {
        UserCreationStatus userCreationStatus;

        try {
            log.info("Sending request to service C");
            ResponseEntity<Void> response = restTemplate.postForEntity("/users", request, Void.class);
            if (response.getStatusCode() == HttpStatus.NO_CONTENT) {
                log.info("Successfully send request to service C");
                userCreationStatus = UserCreationStatus.USER_CREATED;
            } else if (response.getStatusCode() == HttpStatus.CONFLICT) {
                log.warn("User already exists");
                userCreationStatus = UserCreationStatus.USER_ALREADY_EXISTS;
            } else {
                log.error("Unexpected code-response while user registration: {}", response.getStatusCode());
                userCreationStatus = UserCreationStatus.USER_ERROR;
            }
        } catch (HttpStatusCodeException e) {
            log.error("HttpStatusCodeException happened in program: ", e);
            userCreationStatus = UserCreationStatus.USER_ERROR;
        } catch (Exception e) {
            log.error("Something serious happened in program: ", e);
            userCreationStatus = UserCreationStatus.USER_ERROR;
        }

        return userCreationStatus;
    }
}