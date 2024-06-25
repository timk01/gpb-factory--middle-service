package ru.gpb.app.service;

import ru.gpb.app.dto.CreateUserRequest;

public interface UserCommonBackInterface {

    UserCreationStatus createUser(CreateUserRequest request);

    UserRetrievalStatus getUserById(Long usedId);
}
