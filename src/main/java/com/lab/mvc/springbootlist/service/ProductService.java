package com.lab.mvc.springbootlist.service;

import com.lab.mvc.springbootlist.dto.ProductRequestDTO;
import com.lab.mvc.springbootlist.dto.ProductResponseDTO;
import com.lab.mvc.springbootlist.model.Product;
import com.lab.mvc.springbootlist.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Application service: owns product use cases and transaction boundaries. */
@Service
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /** Reads entities and maps them to the DTO consumed by the web layer. */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> listAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    /** Returns one product or raises an explicit not-found error. */
    @Transactional(readOnly = true)
    public ProductResponseDTO get(Long id) {
        return productRepository.findById(id)
                .map(ProductResponseDTO::new)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + id));
    }

    /** Creates a new entity or updates the entity identified by the request ID. */
    public void save(ProductRequestDTO dto) {
        Product product = dto.id() == null
                ? new Product()
                : productRepository.findById(dto.id())
                        .orElseThrow(() -> new EntityNotFoundException(
                                "Produto não encontrado com o ID: " + dto.id()));

        product.setNome(dto.nome());
        product.setMarca(dto.marca());
        product.setPaisFabricacao(dto.paisFabricacao());
        product.setPreco(dto.preco());

        productRepository.save(product);
    }

    /** Looks up the entity before deleting so a missing ID is not silently ignored. */
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Produto não encontrado com o ID: " + id));

        productRepository.delete(product);
    }
}