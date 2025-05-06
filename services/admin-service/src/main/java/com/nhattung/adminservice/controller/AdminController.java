package com.nhattung.adminservice.controller;

import com.nhattung.adminservice.dto.SummaryDto;
import com.nhattung.adminservice.response.ApiResponse;
import com.nhattung.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {


    private final AdminService adminService;

    @GetMapping("/summary")
    public ApiResponse<SummaryDto> getSummary() {
        SummaryDto summary = adminService.getSummary();
        return ApiResponse.<SummaryDto>builder()
                .message("Get summary successfully")
                .result(summary)
                .build();
    }
}
