package com.lab.mvc.springbootlist.service;

import com.lab.mvc.springbootlist.dto.ProductRequestDTO;
import com.lab.mvc.springbootlist.dto.ProductResponseDTO;
import com.lab.mvc.springbootlist.model.Product;
import com.lab.mvc.springbootlist.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    @Test
    void listAllMapsEntitiesToResponseDtos() {
        Product product = product(1L, "Teclado", "Marca", "Brasil", "99.90");
        when(productRepository.findAll()).thenReturn(List.of(product));

        List<ProductResponseDTO> products = productService.listAll();

        assertEquals(1, products.size());
        assertEquals(new ProductResponseDTO(product), products.get(0));
    }

    @Test
    void getReturnsProductWhenItExists() {
        Product product = product(1L, "Teclado", "Marca", "Brasil", "99.90");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertEquals(new ProductResponseDTO(product), productService.get(1L));
    }

    @Test
    void getThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.get(42L));
    }

    @Test
    void saveCreatesProductWhenRequestHasNoId() {
        ProductRequestDTO request = request(null, "Teclado", "Marca", "Brasil", "99.90");
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.save(request);

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());

        Product savedProduct = productCaptor.getValue();
        assertEquals("Teclado", savedProduct.getNome());
        assertEquals("Marca", savedProduct.getMarca());
        assertEquals("Brasil", savedProduct.getPaisFabricacao());
        assertEquals(new BigDecimal("99.90"), savedProduct.getPreco());
    }

    @Test
    void saveUpdatesExistingProduct() {
        Product existing = product(1L, "Antigo", "Marca", "Brasil", "50.00");
        ProductRequestDTO request = request(1L, "Novo", "Marca Nova", "Canadá", "120.50");
        when(productRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        productService.save(request);

        assertEquals("Novo", existing.getNome());
        assertEquals("Marca Nova", existing.getMarca());
        assertEquals("Canadá", existing.getPaisFabricacao());
        assertEquals(new BigDecimal("120.50"), existing.getPreco());
        verify(productRepository).save(existing);
    }

    @Test
    void saveDoesNotCreateProductWhenUpdateIdDoesNotExist() {
        ProductRequestDTO request = request(42L, "Teclado", "Marca", "Brasil", "99.90");
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.save(request));
    }

    @Test
    void deleteRemovesExistingProduct() {
        Product product = product(1L, "Teclado", "Marca", "Brasil", "99.90");
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void deleteThrowsWhenProductDoesNotExist() {
        when(productRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> productService.delete(42L));
    }

    private static Product product(
            Long id,
            String nome,
            String marca,
            String paisFabricacao,
            String preco
    ) {
        return new Product(id, nome, marca, paisFabricacao, new BigDecimal(preco));
    }

    private static ProductRequestDTO request(
            Long id,
            String nome,
            String marca,
            String paisFabricacao,
            String preco
    ) {
        return new ProductRequestDTO(id, nome, marca, paisFabricacao, new BigDecimal(preco));
    }
}
