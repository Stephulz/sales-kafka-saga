package br.com.microservices.orchestrated.authservice.core.repository;

import br.com.microservices.orchestrated.authservice.core.document.Order;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface OrderRepository extends MongoRepository<Order, String> {
}
