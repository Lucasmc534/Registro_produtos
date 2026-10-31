package com.lab.mvc.springbootlist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.lab.mvc.springbootlist.model.Product;

/** Spring Data generates the persistence implementation from this interface. */
public interface ProductRepository extends JpaRepository<Product, Long> {

}
