package com.example.server.controller;

import com.example.server.dto.CommonResponse;
import com.example.server.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

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
    public CommonResponse<Map<String, Object>> recognize(@RequestBody Map<String, String> body) {  //传入json请求
        return CommonResponse.ok(imageService.recognize(body.get("imageUrl")));
    }
}
