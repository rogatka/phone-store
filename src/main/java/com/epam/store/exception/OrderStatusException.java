package com.epam.store.exception;

public class OrderStatusException extends RuntimeException {
    public OrderStatusException() {
    }

    public OrderStatusException(String message) {
        super(message);
    }

    public OrderStatusException(String message, Throwable cause) {
        super(message, cause);
    }

    public OrderStatusException(Throwable cause) {
        super(cause);
    }

    public OrderStatusException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
