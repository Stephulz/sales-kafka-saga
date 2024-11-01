package br.com.microservices.choreography.orderservice.core.service;

import br.com.microservices.choreography.orderservice.config.exception.ValidationException;
import br.com.microservices.choreography.orderservice.core.document.Event;
import br.com.microservices.choreography.orderservice.core.document.History;
import br.com.microservices.choreography.orderservice.core.document.Order;
import br.com.microservices.choreography.orderservice.core.dto.EventFilters;
import br.com.microservices.choreography.orderservice.core.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.LocalDateTime;
import java.util.List;

import static br.com.microservices.choreography.orderservice.core.enums.ESagaStatus.SUCCESS;

@Slf4j
@Service
@AllArgsConstructor
public class EventService {

    private static final String CURRENT_SERVICE = "ORDER_SERVICE";
    private final EventRepository repository;

    public void notifyEnding(Event event) {
        event.setSource(CURRENT_SERVICE);
        event.setOrderId(event.getPayload().getId());
        event.setCreatedAt(LocalDateTime.now());
        setEndingHistory(event);
        save(event);
        log.info("Order {} with saga notified! TransactionId: {}", event.getOrderId(), event.getTransactionId());
    }

    private void setEndingHistory(Event event){
        if(SUCCESS.equals(event.getStatus())){
            log.info("SAGA FINISHED SUCCESSFULLY FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished successfully!");
        } else {
            log.info("SAGA FINISHED WITH ERRORS FOR EVENT {}", event.getId());
            addHistory(event, "Saga finished with errors!");
        }
    }

    public List<Event> findAll() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    public Event findByFilters(EventFilters eventFilters) {
        validadeEmptyFilters(eventFilters);
        if (!ObjectUtils.isEmpty(eventFilters.getOrderId())) {
            return findByOrderId(eventFilters.getOrderId());
        } else {
            return findByTransactionId(eventFilters.getTransactionId());
        }
    }

    private Event findByOrderId(String orderId) {
        return repository.findTop1ByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> new ValidationException("Event not found by orderID."));
    }

    private Event findByTransactionId(String transactionId) {
        return repository.findTop1ByTransactionIdOrderByCreatedAtDesc(transactionId)
                .orElseThrow(() -> new ValidationException("Event not found by transactionID."));
    }

    private void validadeEmptyFilters(EventFilters eventFilters) {
        if (ObjectUtils.isEmpty(eventFilters.getOrderId()) && ObjectUtils.isEmpty(eventFilters.getTransactionId())) {
            throw new ValidationException("OrderID or TransactionID must be informed.");
        }
    }

    public Event save(Event event) {
        return repository.save(event);
    }

    private void addHistory(Event event, String message) {
        final History history = History
                .builder()
                .source(event.getSource())
                .status(event.getStatus())
                .message(message)
                .createdAt(LocalDateTime.now())
                .build();
        event.addToHistory(history);
    }

    public Event createEvent(Order order) {
        final Event event = Event
                .builder()
                .source(CURRENT_SERVICE)
                .status(SUCCESS)
                .orderId(order.getId())
                .transactionId(order.getTransactionId())
                .payload(order)
                .createdAt(LocalDateTime.now())
                .build();
        addHistory(event, "Saga started!");
        return save(event);
    }
}
