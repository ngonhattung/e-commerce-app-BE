package com.nhattung.productservice.service.product;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nhattung.productservice.dto.ImageDto;
import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.dto.ProductSearchCriteria;
import com.nhattung.productservice.entity.Category;
import com.nhattung.productservice.entity.Image;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.exception.AppException;
import com.nhattung.productservice.exception.ErrorCode;
import com.nhattung.productservice.repository.CategoryRepository;
import com.nhattung.productservice.repository.ImageRepository;
import com.nhattung.productservice.repository.ProductRepository;
import com.nhattung.productservice.repository.httpclient.InventoryClient;
import com.nhattung.productservice.request.CreateProductRequest;
import com.nhattung.productservice.request.InventoryRequest;
import com.nhattung.productservice.request.UpdateProductRequest;
import com.nhattung.productservice.response.PageResponse;
import com.nhattung.productservice.utils.ProductSpecification;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService implements IProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ModelMapper modelMapper;
    private final ImageRepository imageRepository;
    private final InventoryClient inventoryClient;
    private final RateLimiter inventoryServiceRateLimiter;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RECENTLY_ADDED_PRODUCTS = "recently_added_products";
    private static final int MAX_RECENT_PRODUCTS = 10;
    @Cacheable(value = "products", key = "#id")
    @Override
    public ProductDto getProductById(Long id) {
        Product product = productRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        return convertToDto(product);
    }

    @Override
    public List<ProductDto> getProductsByIds(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return convertToDtoList(products);
    }

    @Override
    public Map<Long, ProductDto> getProductsByIdsMap(List<Long> ids) {
        List<Product> products = productRepository.findAllById(ids);
        if (products.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }
        return products
                .stream()
                .collect(Collectors.toMap(Product::getId, this::convertToDto));
    }


    @CachePut(value = "products", key = "#request.name")
    @CacheEvict(value = "products", allEntries = true)
    @Override
    public Product saveProduct(CreateProductRequest request) {
        if (isProductExisted(request.getName(), request.getBrand())) {
            throw new AppException(ErrorCode.PRODUCT_EXISTED);
        }
        Category category = Optional.ofNullable(categoryRepository.findByName(request.getCategoryName())).orElseGet(() -> {
            return categoryRepository.save(Category.builder().name(request.getCategoryName()).build());
        });


        Product product = productRepository.save(createProduct(request, category));

        InventoryRequest inventoryRequest = InventoryRequest.builder().productId(product.getId()).quantity(request.getQuantity()).build();
        inventoryClient.addInventory(inventoryRequest);

        return product;
    }

    private boolean isProductExisted(String name, String brand) {
        return productRepository.existsByNameAndBrand(name, brand);
    }

    private Product createProduct(CreateProductRequest request, Category category) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .costPrice(request.getCostPrice())
                .sellingPrice(request.getSellingPrice())
                .category(category)
                .destroyed(false)
                .build();
    }


    @CachePut(value = "products", key = "#id")
    @CacheEvict(value = "products", allEntries = true)
    @Override
    public ProductDto updateProduct(Long id, UpdateProductRequest request) {
        return productRepository.findById(id)
                .map(existingProduct -> updateExistingProduct(existingProduct, request))
                .map(productRepository::save)
                .map(this::convertToDto)  // Chuyển đổi thành DTO trước khi cache
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private Product updateExistingProduct(Product existingProduct, UpdateProductRequest request) {
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

        InventoryRequest inventoryRequest = InventoryRequest.builder().productId(existingProduct.getId()).quantity(request.getQuantity()).build();
        inventoryClient.updateInventory(inventoryRequest);

        return existingProduct;
    }

    @Caching(evict = {@CacheEvict(value = "products", key = "#id"),
            @CacheEvict(value = "products", allEntries = true, beforeInvocation = true)})
    @Override
    public void deleteProduct(Long id) {
        productRepository.findById(id).map(product -> {
            product.setDestroyed(true);
            return productRepository.save(product);
        }).orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }


    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Cacheable(value = "products",key = "'products_page_' + #page + '_size_' + #size")
    @Override
    public PageResponse<ProductDto> getPagedProducts(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Product> productPage = productRepository.findByDestroyedFalse(pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder()
                .currentPage(page)
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .data(productDtos)
                .build();
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByCriteria(ProductSearchCriteria criteria, int page, int size) {
        Specification<ProductDto> specification = ProductSpecification.withSearchCriteria(criteria);
        return getProductDtoPageResponse(page, size, specification);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByCriteriaAndFilter(ProductSearchCriteria criteria, int page, int size) {
        Specification<ProductDto> specification = ProductSpecification.withFilterCriteria(criteria);
        return getProductDtoPageResponse(page, size, specification);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByCriteriaAndFilterHome(ProductSearchCriteria criteria, int page, int size) {
        Specification<ProductDto> specification = ProductSpecification.withFilterCriteriaHome(criteria);
        return getProductDtoPageResponse(page, size, specification);
    }

    private PageResponse<ProductDto> getProductDtoPageResponse(int page, int size, Specification<ProductDto> specification) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> productPage = productRepository.findAll(specification,pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder()
                .currentPage(page)
                .totalPages(productPage.getTotalPages())
                .totalElements(productPage.getTotalElements())
                .pageSize(productPage.getSize())
                .data(productDtos)
                .build();
    }

    @Override
    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryName(category);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByCategory(String category, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Product> productPage = productRepository.findByCategoryName(category, pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder().currentPage(page).totalPages(productPage.getTotalPages()).totalElements(productPage.getTotalElements()).pageSize(productPage.getSize()).data(productDtos).build();
    }

    @Override
    public List<Product> getProductsByBrand(String brand) {
        return productRepository.findByBrand(brand);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByBrand(String brand, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Product> productPage = productRepository.findByBrand(brand, pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder().currentPage(page).totalPages(productPage.getTotalPages()).totalElements(productPage.getTotalElements()).pageSize(productPage.getSize()).data(productDtos).build();
    }

    @Override
    public List<Product> getProductsByCategoryAndBrand(String category, String brand) {
        return productRepository.findByCategoryNameAndBrand(category, brand);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByCategoryAndBrand(String category, String brand, int page, int size) {

        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> productPage = productRepository.findByCategoryNameAndBrand(category, brand, pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder().currentPage(page).totalPages(productPage.getTotalPages()).totalElements(productPage.getTotalElements()).pageSize(productPage.getSize()).data(productDtos).build();

    }

    @Override
    public List<Product> getProductsByName(String name) {
        return productRepository.findByName(name);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByName(String name, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> productPage = productRepository.findByName(name, pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder().currentPage(page).totalPages(productPage.getTotalPages()).totalElements(productPage.getTotalElements()).pageSize(productPage.getSize()).data(productDtos).build();
    }

    @Override
    public List<Product> getProductsByBrandAndName(String brand, String name) {
        return productRepository.findByBrandAndName(brand, name);
    }

    @Override
    public PageResponse<ProductDto> getPagedProductsByBrandAndName(String brand, String name, int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);
        Page<Product> productPage = productRepository.findByBrandAndName(brand, name, pageable);
        List<ProductDto> productDtos = convertToDtoList(productPage.getContent());
        return PageResponse.<ProductDto>builder().currentPage(page).totalPages(productPage.getTotalPages()).totalElements(productPage.getTotalElements()).pageSize(productPage.getSize()).data(productDtos).build();
    }

    @Override
    public Long countProductsByBrandAndName(String brand, String name) {
        return productRepository.countByBrandAndName(brand, name);
    }

    @Override
    public List<ProductDto> getConvertedProducts(List<Product> products) {
        return products.stream().map(this::convertToDto).toList();
    }


    @Override
    public ProductDto convertToDto(Product product) {
        ProductDto productDto = mapperProduct(product);
        int quantity = getInventory(product.getId());
        productDto.setQuantity(quantity);
        return productDto;
    }

    @Override
    public List<ProductDto> convertToDtoList(List<Product> products) {
        // Lấy tất cả productIds và chuyển thành Set để loại bỏ trùng lặp
        Set<Long> productIds = products.stream().map(Product::getId).collect(Collectors.toSet());

        // Lấy thông tin tồn kho cho tất cả sản phẩm trong một lần gọi
        Map<Long, Integer> inventoryMap = inventoryClient.getInventory(productIds);

        // Chuyển đổi các sản phẩm thành DTO, sử dụng dữ liệu tồn kho đã lấy
        return products.stream().map(product -> {
            ProductDto productDto = mapperProduct(product);

            // Lấy quantity từ map đã có thay vì gọi API riêng lẻ
            Integer quantity = inventoryMap.getOrDefault(product.getId(), 0);
            productDto.setQuantity(quantity);

            return productDto;
        }).collect(Collectors.toList());
    }

    @Override
    public long getTotalProductCount() {
        return productRepository.count();
    }


    @Override
    public void addProductRecently(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        ProductDto productDto = convertToDto(product);

        String key = RECENTLY_ADDED_PRODUCTS;

        // Lấy toàn bộ danh sách hiện tại
        List<Object> redisList = redisTemplate.opsForList().range(key, 0, -1);

        // Tạo danh sách mới loại bỏ sản phẩm trùng id
        List<ProductDto> newList = new ArrayList<>();
        if (redisList != null) {
            for (Object item : redisList) {
                if (item instanceof ProductDto dto) {
                    if (!dto.getId().equals(productDto.getId())) {
                        newList.add(dto);
                    }
                } else {
                    // deserialize nếu Redis lưu dưới dạng JSON string
                    ProductDto dto = convertFromRedisObject(item);
                    if (!dto.getId().equals(productDto.getId())) {
                        newList.add(dto);
                    }
                }
            }
        }

        // Thêm sản phẩm mới lên đầu
        newList.add(0, productDto);

        // Giới hạn số lượng sản phẩm gần đây
        if (newList.size() > MAX_RECENT_PRODUCTS) {
            newList = newList.subList(0, MAX_RECENT_PRODUCTS);
        }

        // Xoá danh sách cũ và ghi lại danh sách mới
        redisTemplate.delete(key);
        for (ProductDto dto : newList) {
            redisTemplate.opsForList().rightPush(key, dto);
        }

        // Thiết lập thời gian sống
        redisTemplate.expire(key, Duration.ofHours(1));
    }


    private ProductDto convertFromRedisObject(Object obj) {
        if (obj instanceof String str) {
            try {
                return new ObjectMapper().readValue(str, ProductDto.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to deserialize ProductDto from Redis", e);
            }
        }
        return (ProductDto) obj;
    }
    @Override
    public List<ProductDto> getProductRecently() {
        List<Object> cachedList = redisTemplate.opsForList().range(RECENTLY_ADDED_PRODUCTS, 0, MAX_RECENT_PRODUCTS - 1);
        assert cachedList != null;
        return cachedList.stream()
                .filter(item -> item instanceof ProductDto)
                .map(item -> (ProductDto) item)
                .collect(Collectors.toList());
    }

    private ProductDto mapperProduct(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);
        List<Image> images = imageRepository.findByProductId(product.getId());
        List<ImageDto> imageDtos = images.stream().map(image -> modelMapper.map(image, ImageDto.class)).toList();
        productDto.setImages(imageDtos);
        return productDto;
    }

    public int getInventory(Long productId) {
        Set<Long> idSet = Collections.singleton(productId);
        Map<Long, Integer> inventoryMap = inventoryClient.getInventory(idSet);
        // Gói lời gọi InventoryService trong RateLimiter
        Supplier<Integer> inventorySupplier = RateLimiter.decorateSupplier(
                inventoryServiceRateLimiter,
                () -> inventoryMap.getOrDefault(productId, 0));
        try {
            // Thực thi lời gọi, nếu vượt giới hạn sẽ ném ra lỗi
            return inventorySupplier.get();

        } catch (RequestNotPermitted e) {
            throw new AppException(ErrorCode.RATE_LIMIT_EXCEEDED);
        }

    }


}
