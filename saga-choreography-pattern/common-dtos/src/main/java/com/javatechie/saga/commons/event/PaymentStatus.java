package com.javatechie.saga.commons.event;

public enum PaymentStatus {

    /** Set on the order when it is created; replaced when payment-event is applied */
    PAYMENT_PENDING,
    PAYMENT_COMPLETED,
    PAYMENT_FAILED
}
