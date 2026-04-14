package com.javatechie.saga.order.service;

import com.javatechie.saga.commons.dto.OrderRequestDto;
import com.javatechie.saga.commons.event.OrderEvent;
import com.javatechie.saga.commons.event.OrderStatus;
import com.javatechie.saga.order.config.KafkaConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class OrderStatusPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderStatusPublisher.class);

    @Autowired
    private KafkaTemplate<String, OrderEvent> orderEventKafkaTemplate;

    public void publishOrderEvent(OrderRequestDto orderRequestDto, OrderStatus orderStatus) {
        OrderEvent orderEvent = new OrderEvent(orderRequestDto, orderStatus);
        String key = orderRequestDto.getOrderId() != null
                ? String.valueOf(orderRequestDto.getOrderId())
                : orderEvent.getEventId().toString();
        log.info("Kafka produce (order-service): topic={}, key={}, orderStatus={}", KafkaConfig.ORDER_EVENT_TOPIC, key, orderStatus);
        orderEventKafkaTemplate.send(KafkaConfig.ORDER_EVENT_TOPIC, key, orderEvent).addCallback(new ListenableFutureCallback<SendResult<String, OrderEvent>>() {
            @Override
            public void onSuccess(SendResult<String, OrderEvent> result) {
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
                log.error("Kafka produce failed: topic={}, key={}", KafkaConfig.ORDER_EVENT_TOPIC, key, ex);
            }
        });
    }
}
