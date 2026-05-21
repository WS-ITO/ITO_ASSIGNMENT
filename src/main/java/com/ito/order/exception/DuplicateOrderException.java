package com.ito.order.exception;

public class DuplicateOrderException extends RuntimeException {

    public DuplicateOrderException(String orderId) {
        super("이미 존재하는 주문 ID입니다: " + orderId);
    }
}
