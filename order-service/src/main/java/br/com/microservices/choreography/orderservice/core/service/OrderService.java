package br.com.microservices.choreography.orderservice.core.service;

import br.com.microservices.choreography.orderservice.core.document.Order;
import br.com.microservices.choreography.orderservice.core.dto.OrderRequest;
import br.com.microservices.choreography.orderservice.core.producer.SagaProducer;
import br.com.microservices.choreography.orderservice.core.repository.OrderRepository;
import br.com.microservices.choreography.orderservice.core.utils.JsonUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class OrderService {

    private static final String TRANSACTION_ID_PATTERN = "%s_%s";

    private final EventService eventService;
    private final SagaProducer producer;
    private final JsonUtil jsonUtil;
    private final OrderRepository repository;

    public Order createOrder(OrderRequest orderRequest) {
        final Order order = Order.builder()
                .products(orderRequest.getProducts())
                .createdAt(LocalDateTime.now())
                .transactionId(String.format(TRANSACTION_ID_PATTERN, Instant.now().toEpochMilli(), UUID.randomUUID()))
                .build();
        repository.save(order);
        final String json = jsonUtil.toJson(eventService.createEvent(order));
        producer.sendEvent(json);
        return order;
    }


}
