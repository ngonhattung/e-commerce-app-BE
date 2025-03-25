package com.nhattung.productservice.service.product;

import com.nhattung.productservice.dto.ImageDto;
import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.entity.Category;
import com.nhattung.productservice.entity.Image;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.exception.AppException;
import com.nhattung.productservice.exception.ErrorCode;
import com.nhattung.productservice.repository.CategoryRepository;
import com.nhattung.productservice.repository.ImageRepository;
import com.nhattung.productservice.repository.ProductRepository;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;
    @Override
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Override
    public Product saveProduct(CreateProductRequest request) {
        if (isProductExisted(request.getName(), request.getBrand())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategoryName()))
                .orElseGet(() -> {
                    return categoryRepository.save(Category
                            .builder()
                            .name(request.getCategoryName())
                            .build());
                });
        return productRepository.save(createProduct(request, category));
    }

    private boolean isProductExisted(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }
    private Product createProduct(CreateProductRequest request, Category category) {
        return Product.builder()
                .name(request.getName())
                .brand(request.getBrand())
                .costPrice(request.getCostPrice())
                .sellingPrice(request.getSellingPrice())
                .category(category)
                .build();
    }

    @Override
    public Product updateProduct(Long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map((existingProduct -> updateExistingProduct(existingProduct, request)))
                .map(productRepository::save)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request){
        existingProduct.setName(request.getName());
        existingProduct.setDescription(request.getDescription());
        existingProduct.setBrand(request.getBrand());
        existingProduct.setCostPrice(request.getCostPrice());
        existingProduct.setSellingPrice(request.getSellingPrice());
        Category category = categoryRepository.findByName(request.getCategoryName());
        if (category == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        existingProduct.setCategory(category);
        return existingProduct;
    }
    @Override
    public void deleteProduct(Long id) {
        productRepository.findById(id)
                .ifPresentOrElse(productRepository::delete,() -> {
                    throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
                });
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream()
                .map(image -> modelMapper.map(image, ImageDto.class))
                .toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

}
