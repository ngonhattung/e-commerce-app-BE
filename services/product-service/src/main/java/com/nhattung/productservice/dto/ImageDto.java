package com.nhattung.productservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageDto {
    private Long id;
    private String fileName;
    private String fileType;
    private String fileUri;
}
