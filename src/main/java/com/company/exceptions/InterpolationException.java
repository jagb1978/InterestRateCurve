package com.company.exceptions;

public class InterpolationException extends Exception {
        public InterpolationException() {

        }

        public InterpolationException(String message) {
            super (message);
        }

        public InterpolationException(Throwable cause) {
            super (cause);
        }

        public InterpolationException(String message, Throwable cause) {
            super (message, cause);
        }

}
