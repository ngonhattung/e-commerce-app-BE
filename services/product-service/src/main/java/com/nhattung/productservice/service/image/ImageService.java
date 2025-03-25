package com.nhattung.productservice.service.image;


import com.nhattung.productservice.entity.Image;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ImageService implements IImageService {

    private final ImageRepository imageRepository;
    private final S3Client s3Client;

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
    public List<String> uploadImages(List<MultipartFile> files) {
        List<String> fileUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
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
                                .build(),
                        RequestBody.fromBytes(file.getBytes())
                );

                // Lưu thông tin vào database
                Image image = new Image();
                image.setFileName(fileName);
                image.setFileUri(fileUrl);
                image.setFileType(fileType);
                imageRepository.save(image);

                fileUrls.add(fileUrl);
            } catch (IOException e) {
                throw new AppException(ErrorCode.UPLOAD_IMAGE_ERROR);
            }
        }
        return fileUrls;
    }


}
