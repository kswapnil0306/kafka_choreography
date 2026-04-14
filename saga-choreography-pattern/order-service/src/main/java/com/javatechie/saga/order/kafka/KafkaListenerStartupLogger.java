package com.javatechie.saga.order.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Logs registered Kafka listeners after startup so you can confirm the payment-event consumer is running.
 */
@Component
public class KafkaListenerStartupLogger {

    private static final Logger log = LoggerFactory.getLogger(KafkaListenerStartupLogger.class);

    @Autowired
    private KafkaListenerEndpointRegistry registry;

    @EventListener(ApplicationReadyEvent.class)
    public void onReady() {
        log.info("=== Kafka listener containers (order-service) ===");
        for (MessageListenerContainer container : registry.getListenerContainers()) {
            log.info("listenerId={} running={} paused={}",
                    container.getListenerId(), container.isRunning(), container.isContainerPaused());
        }
        if (registry.getListenerContainers().isEmpty()) {
            log.warn("No Kafka listener containers registered — @KafkaListener beans may be missing or Kafka disabled");
        }
    }
}
