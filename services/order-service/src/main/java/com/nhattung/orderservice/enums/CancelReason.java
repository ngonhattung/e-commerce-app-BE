package com.nhattung.orderservice.enums;

public enum CancelReason {
    INVENTORY_FAILED_AVAILABLE("Some products are not available"),
    INVENTORY_FAILED_AFTER_PAYMENT("Some products are not available after payment"),
    PAYMENT_FAILED("Payment failed"),
    DELIVERY_FAILED("Delivery failed"),
    ;

    CancelReason(String s) {
    }
}
