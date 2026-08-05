package br.com.stephulz.sales.productvalidation.core.repository;

import br.com.stephulz.sales.productvalidation.core.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Boolean existsByCode(String code);
}
