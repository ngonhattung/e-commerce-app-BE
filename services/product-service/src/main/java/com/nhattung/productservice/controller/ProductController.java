package com.nhattung.productservice.controller;

import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.repository.httpclient.InventoryClient;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import com.nhattung.productservice.response.ApiResponse;
import com.nhattung.productservice.service.category.ICategoryService;
import com.nhattung.productservice.service.image.IImageService;
import com.nhattung.productservice.service.product.IProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;
    //private final ICategoryService categoryService;
    private final IImageService imageService;
    //private final RedisTemplate<String, Object> redisTemplate;


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping(value = "/create",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductDto> createProduct(@Valid @ModelAttribute CreateProductRequest request,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files) {
        Product product = productService.saveProduct(request);
        // Save images
        if(files != null && !files.isEmpty()){
            imageService.saveImages(files, product);
        }
        ProductDto productDto = productService.convertToDto(product);
        return ApiResponse.<ProductDto>builder()
                .message("Product created successfully")
                .result(productDto)
                .build();
    }

    @GetMapping("/product/{id}")
    public ApiResponse<ProductDto> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        ProductDto productDto = productService.convertToDto(product);
//        String key = "products::" + id;
//        Object value = redisTemplate.opsForValue().get(key);
//        Product product1 = (Product) value;
        return ApiResponse.<ProductDto>builder()
                .message("Product retrieved successfully")
                .result(productDto)
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @GetMapping("/category/{categoryName}")
    public ApiResponse<List<ProductDto>> getProductsByCategory(@PathVariable String categoryName) {
        List<Product> products = productService.getProductsByCategory(categoryName);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value ="/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductDto> updateProduct(@PathVariable Long id,
                                                 @Valid @ModelAttribute UpdateProductRequest request,
                                                 @RequestParam(value = "imageIds", required = false) List<Long> imageIds,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files
                                                 ) {
        Product product = productService.updateProduct(id, request);
        if(files != null && !files.isEmpty() && imageIds != null){
            imageService.updateImages(imageIds, files);
        }
        ProductDto productDto = productService.convertToDto(product);
        return ApiResponse.<ProductDto>builder()
                .message("Product updated successfully")
                .result(productDto)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/delete/{id}")
    public ApiResponse<String> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ApiResponse.<String>builder()
                .message("Product deleted successfully")
                .result("Product deleted")
                .build();
    }

    @GetMapping("/brand/{brand}")
    public ApiResponse<List<ProductDto>> getProductsByBrand(@PathVariable String brand) {
        List<Product> products = productService.getProductsByBrand(brand);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @GetMapping("/category/{category}/brand/{brand}")
    public ApiResponse<List<ProductDto>> getProductsByCategoryAndBrand(@PathVariable String category,
                                                                       @PathVariable String brand) {
        List<Product> products = productService.getProductsByCategoryAndBrand(category, brand);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @GetMapping("/name/{name}")
    public ApiResponse<List<ProductDto>> getProductsByName(@PathVariable String name) {
        List<Product> products = productService.getProductsByName(name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @GetMapping("/brand/{brand}/name/{name}")
    public ApiResponse<List<ProductDto>> getProductsByBrandAndName(@PathVariable String brand,
                                                                   @PathVariable String name) {
        List<Product> products = productService.getProductsByBrandAndName(brand, name);
        List<ProductDto> productDtos = productService.getConvertedProducts(products);
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productDtos)
                .build();
    }

    @GetMapping("/brand/{brand}/name/{name}/count")
    public ApiResponse<Long> countProductsByBrandAndName(@PathVariable String brand,
                                                         @PathVariable String name) {
        Long count = productService.countProductsByBrandAndName(brand, name);
        return ApiResponse.<Long>builder()
                .message("Products counted successfully")
                .result(count)
                .build();
    }


}
