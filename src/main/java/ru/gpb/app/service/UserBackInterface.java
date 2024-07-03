package ru.gpb.app.service;

import ru.gpb.app.dto.CreateUserRequest;

public interface UserBackInterface {

    UserCreationStatus createUser(CreateUserRequest request);

    UserRetrievalStatus getUserById(Long usedId);
}
