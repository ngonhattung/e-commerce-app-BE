package com.nhattung.userservice.utils;

import com.nhattung.userservice.exception.AppException;
import com.nhattung.userservice.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UploadToS3 {

    private final S3Client s3Client;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    @Value("${aws.s3.bucketName}")
    private String bucketName;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp",
            "video/mp4", "video/mpeg", "video/quicktime", "video/x-msvideo"
    );
    public String uploadAvatarToS3(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
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

            return fileUrl;
        } catch (IOException e) {
            throw new AppException(ErrorCode.UPLOAD_IMAGE_ERROR);
        }
    }
}
