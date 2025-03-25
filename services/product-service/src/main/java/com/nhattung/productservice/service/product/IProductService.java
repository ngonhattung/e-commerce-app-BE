package com.nhattung.productservice.service.product;

import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import org.hibernate.sql.Update;

import java.util.List;

public interface IProductService {
    Product getProductById(Long id);
    Product saveProduct(CreateProductRequest request);
    Product updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);
    List<Product> getAllProducts();
    List<Product> getProductsByCategory(String category);
    List<Product> getProductsByBrand(String brand);
    List<Product> getProductsByCategoryAndBrand(String category, String brand);
    List<Product> getProductsByName(String name);
    List<Product> getProductsByBrandAndName(String brand, String name);
    Long countProductsByBrandAndName(String brand, String name);
    List<ProductDto> getConvertedProducts(List<Product> products);

    ProductDto convertToDto(Product product);
}
