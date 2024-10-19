package com.springboot.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.springboot.Model.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>{

	@Query("SELECT i.imageName FROM Product i")
    List<String> findAllImageNames();
}
