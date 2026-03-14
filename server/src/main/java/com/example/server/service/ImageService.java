package com.example.server.service;

import com.example.server.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageService {

    private final Path uploadPath;
    private final String urlPrefix;

    public ImageService(@Value("${upload.path:./uploads}") String path,
                        @Value("${upload.url-prefix:/uploads}") String urlPrefix) {
        this.uploadPath = Paths.get(path).toAbsolutePath();
        this.urlPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        try {
            Files.createDirectories(this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录", e);
        }
    }

    public String save(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(400, "文件为空");
        }
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString() + (ext != null ? "." + ext : "");
        try {
            Path target = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), target);
            return urlPrefix + "/" + filename;
        } catch (IOException e) {
            log.error("保存文件失败", e);
            throw new BusinessException(500, "保存文件失败");
        }
    }

    private String getExtension(String name) {
        if (name == null || !name.contains(".")) return null;
        return name.substring(name.lastIndexOf('.') + 1);
    }

    /**
     * Mock 图片识别接口，返回模拟的物品类型建议
     */
    public List<String> recognize(String imageUrl) {
        return Arrays.asList("电子产品", "钱包", "证件卡类", "其他");
    }
}
