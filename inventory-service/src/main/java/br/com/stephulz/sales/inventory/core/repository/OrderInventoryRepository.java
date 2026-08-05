package br.com.stephulz.sales.inventory.core.repository;

import br.com.stephulz.sales.inventory.core.model.OrderInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderInventoryRepository extends JpaRepository<OrderInventory, Long> {
    Boolean existsByOrderIdAndTransactionId(String orderId, String transactionId);

    List<OrderInventory> findByOrderIdAndTransactionId(String orderId, String transactionId);
}
