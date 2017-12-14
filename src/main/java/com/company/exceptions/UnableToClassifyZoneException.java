package com.company.exceptions;

public class UnableToClassifyZoneException extends Exception {

    public UnableToClassifyZoneException() {

    }

    public UnableToClassifyZoneException(String message) {
        super(message);
    }

    public UnableToClassifyZoneException(Throwable cause) {
        super(cause);
    }

    public UnableToClassifyZoneException(String message, Throwable cause) {
        super(message, cause);
    }
}
