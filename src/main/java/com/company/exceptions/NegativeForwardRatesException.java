package com.company.exceptions;

public class NegativeForwardRatesException extends Exception {
    public NegativeForwardRatesException() {

    }

    public NegativeForwardRatesException(String message) {
        super(message);
    }

    public NegativeForwardRatesException(Throwable cause) {
        super(cause);
    }

    public NegativeForwardRatesException(String message, Throwable cause) {
        super(message, cause);
    }
}
