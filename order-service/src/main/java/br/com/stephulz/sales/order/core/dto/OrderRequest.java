package br.com.microservices.orchestrated.authservice.core.dto;

import br.com.microservices.orchestrated.authservice.core.document.OrderProducts;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    private List<OrderProducts> products;
}
