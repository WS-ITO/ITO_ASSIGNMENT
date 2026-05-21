package com.ito.order.exception;

public class InvalidOrderException extends RuntimeException {

    public InvalidOrderException(String message) {
        super("유효하지 않은 주문 입력값: " + message);
    }
}
