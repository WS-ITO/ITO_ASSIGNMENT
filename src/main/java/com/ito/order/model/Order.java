package com.ito.order.model;

import java.time.LocalDateTime;

/**
 * 주문 정보를 담는 모델 클래스 (제공 파일 — 수정 불필요)
 *
 * 저장 포맷: orderId|productName|quantity|price|status|createdAt
 */
public class Order {

    private String orderId;
    private String productName;
    private int    quantity;
    private double price;
    private String status;
    private String createdAt;

    public Order() {}

    /** 입력 파일 파싱 후 Order 생성 시 사용 — status/createdAt 자동 설정 */
    public Order(String orderId, String productName, int quantity, double price) {
        this.orderId     = orderId;
        this.productName = productName;
        this.quantity    = quantity;
        this.price       = price;
        this.status      = "PENDING";
        this.createdAt   = LocalDateTime.now().toString();
    }

    // ===== Getters =====
    public String getOrderId()     { return orderId; }
    public String getProductName() { return productName; }
    public int    getQuantity()    { return quantity; }
    public double getPrice()       { return price; }
    public String getStatus()      { return status; }
    public String getCreatedAt()   { return createdAt; }

    // ===== Setters (저장 파일 복원 시 사용) =====
    public void setOrderId(String orderId)         { this.orderId = orderId; }
    public void setProductName(String productName) { this.productName = productName; }
    public void setQuantity(int quantity)          { this.quantity = quantity; }
    public void setPrice(double price)             { this.price = price; }
    public void setStatus(String status)           { this.status = status; }
    public void setCreatedAt(String createdAt)     { this.createdAt = createdAt; }

    /**
     * 저장 파일에 기록할 문자열로 변환합니다.
     * 반환 형식: orderId|productName|quantity|price|status|createdAt
     */
    public String toFileString() {
        return orderId + "|" + productName + "|" + quantity + "|"
             + price   + "|" + status      + "|" + createdAt;
    }

    @Override
    public String toString() {
        return "Order{orderId='" + orderId + "', productName='" + productName
             + "', quantity=" + quantity + ", price=" + price
             + ", status='" + status + "'}";
    }
}
