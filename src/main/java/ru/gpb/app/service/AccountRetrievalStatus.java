package ru.gpb.app.service;

import ru.gpb.app.dto.AccountListResponse;

import java.util.List;

public enum AccountRetrievalStatus {
    ACCOUNTS_FOUND,
    ACCOUNTS_NOT_FOUND,
    ACCOUNTS_ERROR;

    private List<AccountListResponse> accountListResponses;

    public List<AccountListResponse> getAccountListResponses() {
        return accountListResponses;
    }

    public void setAccountListResponses(List<AccountListResponse> accountListResponses) {
        this.accountListResponses = accountListResponses;
    }
}