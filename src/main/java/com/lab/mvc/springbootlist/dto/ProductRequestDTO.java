package com.lab.mvc.springbootlist.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Validated form input. A null ID means creation; a populated ID means update.
 *
 * @param id database identifier, supplied only when editing
 */
public record ProductRequestDTO(
        Long id,

        @NotBlank(message = "O nome do produto é obrigatório.")
        @Size(max = 60, message = "O nome do produto deve ter no máximo 60 caracteres.")
        String nome,

        @NotBlank(message = "A marca é obrigatória.")
        @Size(min = 2, max = 45, message = "A marca deve ter entre 2 e 45 caracteres.")
        String marca,

        @NotBlank(message = "O país de fabricação é obrigatório.")
        @Size(min = 3, max = 60, message = "O país deve ter entre 3 e 60 caracteres.")
        String paisFabricacao,

        @NotNull(message = "O preço é obrigatório.")
        @DecimalMin(value = "0.01", message = "O preço deve ser superior a zero.")
        @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos inteiros e 2 decimais.")
        BigDecimal preco
) {
}
