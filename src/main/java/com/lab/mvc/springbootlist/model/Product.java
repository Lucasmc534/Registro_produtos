package com.lab.mvc.springbootlist.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** JPA entity: one instance corresponds to a row in the products table. */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome do produto é obrigatório")
    @Size(max = 60, message = "O nome do produto deve ter no máximo 60 caracteres")
    @Column(name = "nome", nullable = false, length = 60)
    private String nome;

    @NotBlank(message = "O nome da marca do produto é obrigatório.")
    @Size(min = 2, max = 45, message = "O nome da marca deve ter entre 2 e 45 caracteres.")
    @Column(name = "marca", nullable = false, length = 45)
    private String marca;

    @NotBlank(message = "O nome do país onde foi fabricado é obrigatório")
    @Size(min = 3, message = "O nome do país deve ter no mínimo 3 caracteres")
    @Column(name = "pais_fabricacao", nullable = false, length = 60)
    private String paisFabricacao;

    @NotNull(message = "O preço é obrigatório.")
    @DecimalMin(value = "0.01", message = "O preço deve ser superior a zero.")
    @Digits(integer = 8, fraction = 2, message = "O preço deve ter até 8 dígitos inteiros e 2 decimais.")
    @Column(name = "preco", nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;
}