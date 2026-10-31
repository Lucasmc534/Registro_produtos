
package com.lab.mvc.springbootlist.controller;

import com.lab.mvc.springbootlist.dto.ProductRequestDTO;
import com.lab.mvc.springbootlist.dto.ProductResponseDTO;
import com.lab.mvc.springbootlist.service.ProductService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** Web adapter: maps HTTP requests to service operations and Thymeleaf views. */
@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /** Loads products into the view model and renders the catalog page. */
    @GetMapping
    public String listProducts(Model model) {
        model.addAttribute("listProducts", productService.listAll());
        return "index";
    }

    /** Supplies an empty request DTO to the product creation form. */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new ProductRequestDTO(null, "", "", "", null));
        return "new_product";
    }

    /** Validates submitted fields before asking the service to persist them. */
    @PostMapping("/save")
    public String saveProduct(
            @Valid @ModelAttribute("product") ProductRequestDTO product,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            if (product.id() == null) {
                return "new_product";
            }
            return "edit_product";
        }

        productService.save(product);
        redirectAttributes.addFlashAttribute("success", "Registro salvo com sucesso!");
        return "redirect:/products";
    }

    /** Loads an existing product and adapts it to the form's request DTO. */
    @GetMapping("/edit/{id}")
    public String showEditProductPage(@PathVariable("id") Long id, Model model) {
        ProductResponseDTO product = productService.get(id);
        model.addAttribute("product", new ProductRequestDTO(
                product.id(),
                product.nome(),
                product.marca(),
                product.paisFabricacao(),
                product.preco()
        ));
        return "edit_product";
    }

    /** Deletes through POST so the action is protected by Spring Security CSRF. */
    @PostMapping("/delete/{id}")
    public String deleteProduct(
            @PathVariable("id") Long id,
            RedirectAttributes redirectAttributes) {
        productService.delete(id);
        redirectAttributes.addFlashAttribute("success", "Registro excluído com sucesso!");
        return "redirect:/products";
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public String handleProductNotFound(EntityNotFoundException exception) {
        return exception.getMessage();
    }
}