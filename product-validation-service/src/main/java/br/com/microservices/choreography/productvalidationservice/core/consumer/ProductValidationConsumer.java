package br.com.microservices.choreography.productvalidationservice.core.consumer;

import br.com.microservices.choreography.productvalidationservice.core.dto.Event;
import br.com.microservices.choreography.productvalidationservice.core.service.ProductValidationService;
import br.com.microservices.choreography.productvalidationservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class ProductValidationConsumer {

    private final ProductValidationService productValidationService;
    private final JsonUtil jsonUtil;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-start}"
    )
    public void consumeSuccessEvent(String payload) {
        log.info("Receiving success event {} from product-validation-start topic", payload);
        final Event event = jsonUtil.toEvent(payload);
        productValidationService.validateExistingProducts(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.product-validation-fail}"
    )
    public void consumeFailEvent(String payload) {
        log.info("Receiving rollback event {} from product-validation-fail topic", payload);
        final Event event = jsonUtil.toEvent(payload);
        productValidationService.rollBackEvent(event);
    }
}
