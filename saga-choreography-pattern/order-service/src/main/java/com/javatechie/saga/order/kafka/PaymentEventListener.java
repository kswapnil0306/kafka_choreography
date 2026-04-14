package com.javatechie.saga.order.kafka;

import com.javatechie.saga.commons.event.PaymentEvent;
import com.javatechie.saga.order.config.KafkaConfig;
import com.javatechie.saga.order.config.OrderStatusUpdateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);

    @Autowired
    private OrderStatusUpdateHandler handler;

    @KafkaListener(
            topics = KafkaConfig.PAYMENT_EVENT_TOPIC,
            containerFactory = "paymentEventListenerContainerFactory")
    @Transactional
    public void onPaymentEvent(PaymentEvent payment) {
        Integer orderId = payment.getPaymentRequestDto() != null ? payment.getPaymentRequestDto().getOrderId() : null;
        log.info("Kafka listen (order-service): topic={}, orderId={}, paymentStatus={}, eventId={}",
                KafkaConfig.PAYMENT_EVENT_TOPIC, orderId, payment.getPaymentStatus(),
                payment.getEventId());
        if (orderId == null) {
            log.warn("Skipping payment event: missing orderId in payload");
            return;
        }
        handler.updateOrder(orderId, po -> po.setPaymentStatus(payment.getPaymentStatus()));
    }
}
