package ru.gpb.app.service;

import ru.gpb.app.dto.CreateAccountRequest;

public interface AccountBackInterface {

    AccountCreationStatus createAccount(CreateAccountRequest request);

    AccountRetrievalStatus getAccountsById(Long userId);
}
