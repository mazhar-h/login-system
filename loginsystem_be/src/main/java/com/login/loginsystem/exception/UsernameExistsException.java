package com.login.loginsystem.exception;

public class UsernameExistsException extends RuntimeException {
    private String message;

    public UsernameExistsException() {}

    public UsernameExistsException(String msg) {
        super(msg);
        this.message = msg;
    }
}
