package com.krasnovm.FirstTryBot.exception;

public class ServiceException extends Exception{
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
