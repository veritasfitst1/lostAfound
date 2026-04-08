package com.example.server.service;

import com.example.server.entity.ItemCategory;
import com.example.server.exception.BusinessException;
import com.example.server.repository.ItemCategoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageService {

    private final Path uploadPath;
    private final String urlPrefix;
    private final BaiduAiService baiduAiService;
    private final ItemCategoryRepository categoryRepository;

    public ImageService(@Value("${upload.path:./uploads}") String path,
                        @Value("${upload.url-prefix:/uploads}") String urlPrefix,
                        BaiduAiService baiduAiService,
                        ItemCategoryRepository categoryRepository) {
        this.uploadPath = Paths.get(path).toAbsolutePath();
        this.urlPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        this.baiduAiService = baiduAiService;
        this.categoryRepository = categoryRepository;
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
     * 图片识别：读取已上传的图片 → 调百度 AI → 关键词匹配数据库分类 → 返回结果
     */
    public Map<String, Object> recognize(String imageUrl) {
        Path imagePath = uploadPath.resolve(imageUrl.replace(urlPrefix + "/", ""));  //文件路径-》本地路径
        byte[] imageBytes;   //读取文件
        try {
            imageBytes = Files.readAllBytes(imagePath);
        } catch (IOException e) {
            log.error("读取图片文件失败: {}", imagePath, e);
            throw new BusinessException(400, "图片文件不存在");
        }

        List<String> keywords = baiduAiService.recognize(imageBytes);
        log.info("百度AI识别结果: {}", keywords);

        Long suggestedCategoryId = matchCategory(keywords);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keywords", keywords);
        result.put("suggestedCategoryId", suggestedCategoryId);
        return result;
    }

    /**
     * 将百度识别的关键词与数据库分类名进行模糊匹配，返回最佳匹配的分类 ID
     */
    private Long matchCategory(List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) return null;
        List<ItemCategory> categories = categoryRepository.findAllByOrderBySortOrderAsc();
        String joined = keywords.stream().map(String::toLowerCase).collect(Collectors.joining(" "));
        for (ItemCategory cat : categories) {
            String catName = cat.getName().toLowerCase();
            if (joined.contains(catName) || catName.contains(joined)) {
                return cat.getId();
            }
            for (String kw : keywords) {
                if (kw.toLowerCase().contains(catName) || catName.contains(kw.toLowerCase())) {
                    return cat.getId();
                }
            }
        }
        return null;
    }
}
