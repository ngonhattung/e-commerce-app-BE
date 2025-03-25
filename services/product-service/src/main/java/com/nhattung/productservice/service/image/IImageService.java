package com.nhattung.productservice.service.image;

import com.nhattung.productservice.dto.ImageDto;
import com.nhattung.productservice.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IImageService {
    Image getImageById(Long id);
    List<Image> getImages();
    List<String> uploadImages(List<MultipartFile> files);
}
