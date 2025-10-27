package com.example.decathlon.common;

//A new Exception
public class InvalidResultException extends Exception {
    public InvalidResultException(String message) {
        super(message);
    }
}