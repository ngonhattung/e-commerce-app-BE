package com.nhattung.productservice.service.product;

import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.dto.ProductSearchCriteria;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import com.nhattung.productservice.response.PageResponse;
import org.hibernate.sql.Update;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

public interface IProductService {
    ProductDto getProductById(Long id);
    List<ProductDto> getProductsByIds(List<Long> ids);
    Map<Long, ProductDto> getProductsByIdsMap(List<Long> ids);
    Product saveProduct(CreateProductRequest request);
    ProductDto updateProduct(Long id, UpdateProductRequest request);
    void deleteProduct(Long id);

    List<Product> getAllProducts();
    PageResponse<ProductDto> getPagedProducts(int page, int size);
    PageResponse<ProductDto> getPagedProductsByCriteria(ProductSearchCriteria criteria, int page, int size);
    PageResponse<ProductDto> getPagedProductsByCriteriaAndFilter(ProductSearchCriteria criteria, int page, int size);
    PageResponse<ProductDto> getPagedProductsByCriteriaAndFilterHome(ProductSearchCriteria criteria, int page, int size);

    List<Product> getProductsByCategory(String category);
    PageResponse<ProductDto> getPagedProductsByCategory(String category, int page, int size);

    List<Product> getProductsByBrand(String brand);
    PageResponse<ProductDto> getPagedProductsByBrand(String brand, int page, int size);

    List<Product> getProductsByCategoryAndBrand(String category, String brand);
    PageResponse<ProductDto> getPagedProductsByCategoryAndBrand(String category, String brand, int page, int size);

    List<Product> getProductsByName(String name);
    PageResponse<ProductDto> getPagedProductsByName(String name, int page, int size);

    List<Product> getProductsByBrandAndName(String brand, String name);
    PageResponse<ProductDto> getPagedProductsByBrandAndName(String brand, String name, int page, int size);

    Long countProductsByBrandAndName(String brand, String name);
    List<ProductDto> getConvertedProducts(List<Product> products);
    ProductDto convertToDto(Product product);
    List<ProductDto> convertToDtoList(List<Product> products);

    long getTotalProductCount();


    void addProductRecently(Long id);
    List<ProductDto> getProductRecently();
}
