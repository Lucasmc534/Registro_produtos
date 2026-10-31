package com.lab.mvc.springbootlist.dto;

import com.lab.mvc.springbootlist.model.Product;

import java.math.BigDecimal;

/** Read-only product data passed from the service to the web presentation layer. */
public record ProductResponseDTO(
        Long id,
        String nome,
        String marca,
        String paisFabricacao,
        BigDecimal preco
) {
    /** Copies persisted entity values into the DTO without exposing the entity itself. */
    public ProductResponseDTO(Product product) {
        this(
                product.getId(),
                product.getNome(),
                product.getMarca(),
                product.getPaisFabricacao(),
                product.getPreco()
        );
    }
}
