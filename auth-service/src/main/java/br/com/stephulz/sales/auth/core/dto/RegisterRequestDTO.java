package br.com.stephulz.sales.auth.core.dto;

public record RegisterRequestDTO(
        String email,
        String password
) {
}
