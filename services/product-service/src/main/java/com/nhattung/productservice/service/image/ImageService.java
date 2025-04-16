package com.nhattung.productservice.service.image;


import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.entity.Image;
import com.nhattung.productservice.entity.Product;
import com.nhattung.productservice.exception.AppException;
import com.nhattung.productservice.exception.ErrorCode;
import com.nhattung.productservice.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;
    private final UUID uuid;
    @Value("${aws.s3.bucketName}")
    private String bucketName;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo"
    );

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.IMAGE_NOT_FOUND));
    }

    @Override
    public List<Image> getImages() {
        return imageRepository.findAll();
    }

    @Override
    public void saveImages(List<MultipartFile> files, Product product) {

        for (MultipartFile file : files) {
            String fileName = uuid.toString() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String fileType = file.getContentType();
            String fileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + fileName;


            if (file.getSize() > MAX_FILE_SIZE) {
                throw new AppException(ErrorCode.FILE_SIZE_TOO_LARGE);
            }

            if (!ALLOWED_MIME_TYPES.contains(file.getContentType())) {
                throw new AppException(ErrorCode.INVALID_FILE_TYPE);
            }

            try {
                // Upload file lên S3
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(fileName)
                                .contentType(fileType)
                                .build(),
                        RequestBody.fromBytes(file.getBytes())
                );

                // Lưu thông tin vào database
                Image image = Image.builder()
                        .fileName(fileName)
                        .fileUri(fileUrl)
                        .fileType(fileType)
                        .product(product)
                        .build();
                imageRepository.save(image);

            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_IMAGE_ERROR);
            }
        }
    }

    @Override
    public void updateImages(List<Long> imageIdsToUpdate, List<MultipartFile> newFiles) {

        if (imageIdsToUpdate.size() != newFiles.size()) {
            throw new AppException(ErrorCode.FILE_SIZE_MISMATCH);
        }

        for (int i = 0; i < imageIdsToUpdate.size(); i++) {
            Long imageId = imageIdsToUpdate.get(i);
            MultipartFile newFile = newFiles.get(i);

            Optional<Image> imageOptional = imageRepository.findById(imageId);
            if (imageOptional.isPresent()) {
                Image image = imageOptional.get();

                // Upload ảnh mới lên S3
                try {
                    String newFileName = uuid.toString() + "_" + System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
                    String newFileUrl = "https://" + bucketName + ".s3.amazonaws.com/" + newFileName;
                    String fileType = newFile.getContentType();
                    s3Client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(newFileName)
                                    .contentType(newFile.getContentType())
                                    .build(),
                            RequestBody.fromBytes(newFile.getBytes())
                    );

                    // Cập nhật đường dẫn trong database
                    image.setFileName(newFileName);
                    image.setFileType(fileType);
                    image.setFileUri(newFileUrl);
                    imageRepository.save(image);
                } catch (IOException e) {
                    throw new AppException(ErrorCode.UPLOAD_IMAGE_ERROR);
                }
            }
        }
    }

}
