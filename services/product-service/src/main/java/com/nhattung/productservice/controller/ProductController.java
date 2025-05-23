package com.nhattung.productservice.controller;

import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.dto.ProductSearchCriteria;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.repository.httpclient.InventoryClient;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import com.nhattung.productservice.response.ApiResponse;
import com.nhattung.productservice.response.PageResponse;
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

import java.math.BigDecimal;
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

        return ApiResponse.<ProductDto>builder()
                .message("Product retrieved successfully")
                .result(productService.getProductById(id))
                .build();
    }

    @GetMapping("/productsByIds")
    public ApiResponse<List<ProductDto>> getProductsByIds(@RequestParam List<Long> ids) {
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getProductsByIds(ids))
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<PageResponse<ProductDto>> getAllProducts(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {

        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getPagedProducts(page, size))
                .build();
    }

    @GetMapping("/category")
    public ApiResponse<PageResponse<ProductDto>> getProductsByCategory(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryName") String categoryName
    ) {
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getPagedProductsByCategory(categoryName, page, size))
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping(value ="/update/{id}",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProductDto> updateProduct(@PathVariable Long id,
                                                 @Valid @ModelAttribute UpdateProductRequest request,
                                                 @RequestParam(value = "imageIds", required = false) List<Long> imageIds,
                                                 @RequestParam(value = "files", required = false) List<MultipartFile> files
                                                 ) {
        if(files != null && !files.isEmpty() && imageIds != null){
            imageService.updateImages(imageIds, files);
        }
        return ApiResponse.<ProductDto>builder()
                .message("Product updated successfully")
                .result(productService.updateProduct(id, request))
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

    @GetMapping("/brand")
    public ApiResponse<PageResponse<ProductDto>> getProductsByBrand(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "brandName") String brand
    ) {
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getPagedProductsByBrand(brand, page, size))
                .build();
    }

    @GetMapping("/category/brand")
    public ApiResponse<PageResponse<ProductDto>> getProductsByCategoryAndBrand(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "categoryName") String categoryName,
            @RequestParam(value = "brandName") String brand
    ) {
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getPagedProductsByCategoryAndBrand(categoryName, brand, page, size))
                .build();
    }

    @GetMapping("/name")
    public ApiResponse<List<ProductDto>> getProductsByName(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "name") String name) {
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getConvertedProducts(productService.getProductsByName(name)))
                .build();
    }

    @GetMapping("/brand/name")
    public ApiResponse<List<ProductDto>> getProductsByBrandAndName(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "brandName") String brand,
            @RequestParam(value = "name") String name) {
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getConvertedProducts(productService.getProductsByBrandAndName(brand, name)))
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


    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/count")
    public ApiResponse<Long> getTotalProductCount() {
        long count = productService.getTotalProductCount();
        return ApiResponse.<Long>builder()
                .message("Total product count retrieved successfully")
                .result(count)
                .build();
    }

    @GetMapping("/getByIds")
    public ApiResponse<List<ProductDto>> getProductsByIdsMap(@RequestParam List<Long> ids) {
        return ApiResponse.<List<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productService.getProductsByIdsMap(ids).values().stream().toList())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductDto>> searchProducts(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .searchTerm(searchTerm)
                .build();

        PageResponse<ProductDto> productPage = productService.getPagedProductsByCriteria(criteria, page, size);
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productPage)
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<PageResponse<ProductDto>> filterProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .name(name)
                .brand(brand)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        PageResponse<ProductDto> productPage = productService.getPagedProductsByCriteriaAndFilter(criteria, page, size);
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productPage)
                .build();
    }

    @GetMapping("/filter/home")
    public ApiResponse<PageResponse<ProductDto>> filterProductsHome(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        ProductSearchCriteria criteria = ProductSearchCriteria.builder()
                .searchTerm(searchTerm)
                .categoryName(categoryName)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .build();

        PageResponse<ProductDto> productPage = productService.getPagedProductsByCriteriaAndFilterHome(criteria, page, size);
        return ApiResponse.<PageResponse<ProductDto>>builder()
                .message("Products retrieved successfully")
                .result(productPage)
                .build();
    }

    @PostMapping("/recently/add/{id}")
    public ApiResponse<Void> addProductRecently(@PathVariable(value = "id") Long id) {
        productService.addProductRecently(id);
        return ApiResponse.<Void>builder()
                .message("Product added to recently viewed successfully")
                .build();
    }

    @GetMapping("/recently/get")
    public ApiResponse<List<ProductDto>> getProductRecently() {
        List<ProductDto> productDtos = productService.getProductRecently();
        return ApiResponse.<List<ProductDto>>builder()
                .message("Recently viewed products retrieved successfully")
                .result(productDtos)
                .build();
    }

}
