package ru.gpb.app.service;

import ru.gpb.app.dto.CreateAccountRequest;

public interface AccountCommonBackInterface {

    AccountCreationStatus createAccount(CreateAccountRequest request);

    AccountRetrievalStatus getAccountsById(Long userId);
}
