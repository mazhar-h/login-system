package com.login.loginsystem.exception;

public class AccountLinkedException extends RuntimeException {
    private String message;

    public AccountLinkedException() {}

    public AccountLinkedException(String msg) {
        super(msg);
        this.message = msg;
    }
}
