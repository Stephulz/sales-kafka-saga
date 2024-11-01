package br.com.microservices.choreography.orderservice.core.consumer;

import br.com.microservices.choreography.orderservice.core.utils.JsonUtil;
import br.com.microservices.choreography.orderservice.core.document.Event;
import br.com.microservices.choreography.orderservice.core.service.EventService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class EventConsumer {
    private final JsonUtil jsonUtil;
    private final EventService service;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.notify-ending}"
    )
    public void consumeNotifyEndingEvent(String payload) {
        log.info("Receiving ending notification event {} from notify-ending topic", payload);
        final Event event = jsonUtil.toEvent(payload);
        service.notifyEnding(event);
    }
}
