package com.nhattung.productservice.service.image;

import com.nhattung.productservice.dto.ImageDto;
import com.nhattung.productservice.entity.Image;
import com.nhattung.productservice.entity.Product;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);
    List<Image> getImages();
    void saveImages(List<MultipartFile> files, Product product);
    //void updateImages(List<MultipartFile> files, Product product);
}
