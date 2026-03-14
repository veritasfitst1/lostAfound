package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public CommonResponse<String> upload(@RequestParam("file") MultipartFile file) {
        return CommonResponse.ok(imageService.save(file));
    }

    @PostMapping("/recognize")
    public CommonResponse<List<String>> recognize(@RequestParam("imageUrl") String imageUrl) {
        return CommonResponse.ok(imageService.recognize(imageUrl));
    }
}
