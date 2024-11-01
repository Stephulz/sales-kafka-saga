package br.com.microservices.choreography.inventoryservice.core.service;

import br.com.microservices.choreography.inventoryservice.config.exception.ValidationException;
import br.com.microservices.choreography.inventoryservice.core.dto.Event;
import br.com.microservices.choreography.inventoryservice.core.dto.History;
import br.com.microservices.choreography.inventoryservice.core.dto.Order;
import br.com.microservices.choreography.inventoryservice.core.dto.OrderProducts;
import br.com.microservices.choreography.inventoryservice.core.model.Inventory;
import br.com.microservices.choreography.inventoryservice.core.model.OrderInventory;
import br.com.microservices.choreography.inventoryservice.core.repository.InventoryRepository;
import br.com.microservices.choreography.inventoryservice.core.repository.OrderInventoryRepository;
import br.com.microservices.choreography.inventoryservice.core.saga.SagaExecutionController;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

import static br.com.microservices.choreography.inventoryservice.core.enums.ESagaStatus.FAIL;
import static br.com.microservices.choreography.inventoryservice.core.enums.ESagaStatus.ROLLBACK_PEDING;
import static br.com.microservices.choreography.inventoryservice.core.enums.ESagaStatus.SUCCESS;

@Slf4j
@Service
@AllArgsConstructor
public class InventoryService {
    private static final String CURRENT_SOURCE = "INVENTORY_SERVICE";

    private final InventoryRepository inventoryRepository;
    private final OrderInventoryRepository orderInventoryRepository;
    private final SagaExecutionController sagaExecutionController;

    public void updateInventory(Event event) {
        try {
            checkCurrentValidation(event);
            createOrderInventory(event);
            updateInventory(event.getPayload());
            handleSuccess(event);
        } catch (Exception e) {
            log.error("Error trying to update inventory: ", e);
            handleFailCurrentNotExecuted(event, e.getMessage());
        }
        sagaExecutionController.handleSaga(event);
    }

    private void checkCurrentValidation(Event event) {
        if (Boolean.TRUE.equals(orderInventoryRepository.existsByOrderIdAndTransactionId(
                event.getOrderId(), event.getTransactionId()
        ))) {
            throw new ValidationException("There's another transactionId for this validation.");
        }
    }

    private void createOrderInventory(Event event) {
        event
                .getPayload()
                .getProducts()
                .forEach(product -> {
                    final Inventory inventory = findInventoryByProductCode(product.getProduct().getCode());
                    final OrderInventory orderInventory = createOrderInventory(event, product, inventory);
                    orderInventoryRepository.save(orderInventory);
                });
    }

    private OrderInventory createOrderInventory(Event event, OrderProducts product, Inventory inventory) {
        return OrderInventory
                .builder()
                .orderId(event.getPayload().getId())
                .transactionId(event.getTransactionId())
                .inventory(inventory)
                .oldQuantity(inventory.getAvailable())
                .orderQuantity(product.getQuantity())
                .newQuantity(inventory.getAvailable() - product.getQuantity())
                .build();
    }

    private void updateInventory(Order order) {
        order
                .getProducts()
                .forEach(product -> {
                    final Inventory inventory = findInventoryByProductCode(product.getProduct().getCode());
                    checkInventory(inventory.getAvailable(), product.getQuantity());
                    inventory.setAvailable(inventory.getAvailable() - product.getQuantity());
                    inventoryRepository.save(inventory);
                });
    }

    private void checkInventory(int available, int orderQuantity) {
        if (orderQuantity > available) {
            throw new ValidationException("Product is out of stock!");
        }
    }

    private void handleSuccess(Event event) {
        event.setStatus(SUCCESS);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Inventory updated successfully!");
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

    private void handleFailCurrentNotExecuted(Event event, String message) {
        event.setStatus(ROLLBACK_PEDING);
        event.setSource(CURRENT_SOURCE);
        addHistory(event, "Fail to update inventory: ".concat(message));
    }

    public void rollbackInventory(Event event) {
        event.setStatus(FAIL);
        event.setSource(CURRENT_SOURCE);
        try {
            returnInventoryToPreviousValues(event);
            addHistory(event, "Rollback executed on inventory!");
        } catch (Exception e) {
            addHistory(event, "Rollback not executed on inventory: ".concat(e.getMessage()));
        }
        sagaExecutionController.handleSaga(event);
    }

    private void returnInventoryToPreviousValues(Event event) {
        orderInventoryRepository
                .findByOrderIdAndTransactionId(event.getPayload().getId(), event.getTransactionId())
                .forEach(orderInventory -> {
                    final Inventory inventory = orderInventory.getInventory();
                    inventory.setAvailable(orderInventory.getOldQuantity());
                    inventoryRepository.save(inventory);
                    log.info("Restored inventory for order {} from {} to {}",
                            event.getPayload().getId(), orderInventory.getNewQuantity(), inventory.getAvailable());
                });
    }

    private Inventory findInventoryByProductCode(String productCode) {
        return inventoryRepository
                .findByProductCode(productCode)
                .orElseThrow(() -> new ValidationException("Inventory not found by informed product."));
    }
}
