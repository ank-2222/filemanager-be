package com.file.manager.exception;

public class AuthExcpetion extends RuntimeException{

    public AuthExcpetion(String message) {
        super(message);
    }

    public AuthExcpetion(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthExcpetion(Throwable cause) {
        super(cause);
    }


}
