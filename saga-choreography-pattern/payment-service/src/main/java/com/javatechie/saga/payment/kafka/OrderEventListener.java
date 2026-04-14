package com.javatechie.saga.payment.kafka;

import com.javatechie.saga.commons.event.OrderEvent;
import com.javatechie.saga.commons.event.OrderStatus;
import com.javatechie.saga.commons.event.PaymentEvent;
import com.javatechie.saga.payment.config.KafkaConfig;
import com.javatechie.saga.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private KafkaTemplate<String, PaymentEvent> paymentEventKafkaTemplate;

    @KafkaListener(
            topics = KafkaConfig.ORDER_EVENT_TOPIC,
            containerFactory = "orderEventListenerContainerFactory")
    public void onOrderEvent(OrderEvent orderEvent) {
        Integer orderId = orderEvent.getOrderRequestDto() != null ? orderEvent.getOrderRequestDto().getOrderId() : null;
        log.info("Kafka listen (payment-service): topic={}, orderId={}, orderStatus={}, eventId={}",
                KafkaConfig.ORDER_EVENT_TOPIC, orderId, orderEvent.getOrderStatus(), orderEvent.getEventId());
        if (OrderStatus.ORDER_CREATED.equals(orderEvent.getOrderStatus())) {
            PaymentEvent paymentEvent = paymentService.newOrderEvent(orderEvent);
            String key = String.valueOf(paymentEvent.getPaymentRequestDto().getOrderId());
            log.info("Kafka produce (payment-service): topic={}, key={}, paymentStatus={}",
                    KafkaConfig.PAYMENT_EVENT_TOPIC, key, paymentEvent.getPaymentStatus());
            paymentEventKafkaTemplate.send(KafkaConfig.PAYMENT_EVENT_TOPIC, key, paymentEvent)
                    .addCallback(new ListenableFutureCallback<SendResult<String, PaymentEvent>>() {
                        @Override
                        public void onSuccess(SendResult<String, PaymentEvent> result) {
                            if (result != null && result.getRecordMetadata() != null) {
                                log.info("Kafka produced OK: topic={}, partition={}, offset={}, key={}",
                                        result.getRecordMetadata().topic(),
                                        result.getRecordMetadata().partition(),
                                        result.getRecordMetadata().offset(),
                                        key);
                            }
                        }

                        @Override
                        public void onFailure(Throwable ex) {
                            log.error("Kafka produce failed: topic={}, key={}", KafkaConfig.PAYMENT_EVENT_TOPIC, key, ex);
                        }
                    });
        } else {
            paymentService.cancelOrderEvent(orderEvent);
        }
    }
}
