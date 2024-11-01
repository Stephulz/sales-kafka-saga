package br.com.microservices.choreography.paymentservice.core.consumer;

import br.com.microservices.choreography.paymentservice.core.dto.Event;
import br.com.microservices.choreography.paymentservice.core.service.PaymentService;
import br.com.microservices.choreography.paymentservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@AllArgsConstructor
public class PaymentConsumer {
    private final JsonUtil jsonUtil;
    private final PaymentService paymentService;

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-success}"
    )
    public void consumeSuccessEvent(String payload) {
        log.info("Receiving success event {} from payment-success topic", payload);
        final Event event = jsonUtil.toEvent(payload);
        paymentService.realizePayment(event);
    }

    @KafkaListener(
            groupId = "${spring.kafka.consumer.group-id}",
            topics = "${spring.kafka.topic.payment-fail}"
    )
    public void consumeFailEvent(String payload) {
        log.info("Receiving rollback event {} from payment-fail topic", payload);
        final Event event = jsonUtil.toEvent(payload);
        paymentService.realizeRefund(event);
    }
}
