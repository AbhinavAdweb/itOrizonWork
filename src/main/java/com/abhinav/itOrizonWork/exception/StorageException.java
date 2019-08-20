package com.abhinav.itOrizonWork.exception;

// Custom exception class to map it properly with our view
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}