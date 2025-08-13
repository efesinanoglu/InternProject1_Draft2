package com.example.contents.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}