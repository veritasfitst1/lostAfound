package com.example.server.service;

import com.example.server.dto.ItemVO;
import com.example.server.dto.PageResponse;
import com.example.server.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class ImageService {

    private final Path uploadPath;
    private final String urlPrefix;
    private final BaiduAiService baiduAiService;
    private final ItemService itemService;

    public ImageService(@Value("${upload.path:./uploads}") String path,
                        @Value("${upload.url-prefix:/uploads}") String urlPrefix,
                        BaiduAiService baiduAiService,
                        ItemService itemService) {
        this.uploadPath = Paths.get(path).toAbsolutePath();
        this.urlPrefix = urlPrefix.endsWith("/") ? urlPrefix.substring(0, urlPrefix.length() - 1) : urlPrefix;
        this.baiduAiService = baiduAiService;
        this.itemService = itemService;
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
     * 图片识别：读取已上传的图片 → 调百度 AI → 使用关键词进行物品筛选 → 返回结果
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
        //log.info("百度AI识别结果: {}", keywords);

        // 组合成搜索用的 keyword（取前 3 个，空格拼接）
        String searchKeyword = "";
        if (keywords != null && !keywords.isEmpty()) {
            int limit = Math.min(3, keywords.size());
            searchKeyword = String.join(" ", keywords.subList(0, limit));
        }

        // 复用已有搜索函数：按 title/description/location/categoryName 模糊匹配
        PageResponse<ItemVO> page = itemService.list(
                searchKeyword.isBlank() ? null : searchKeyword,
                null,
                null,
                null,
                0,
                20
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("keywords", keywords);
        result.put("items", page.getContent());
        result.put("total", page.getTotal());
        return result;
    }
}
