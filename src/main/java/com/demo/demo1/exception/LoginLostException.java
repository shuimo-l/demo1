package com.demo.demo1.exception;

/**
 * @author: liuxl
 * @date: 2018-11-06 17:22
 * @description:
 */
public class LoginLostException extends RuntimeException {
    public LoginLostException() {

    }

    public LoginLostException(String message) {
        super(message);
    }
}