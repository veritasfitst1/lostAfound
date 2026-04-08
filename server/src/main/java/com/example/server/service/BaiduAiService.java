package com.example.server.service;

import com.example.server.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BaiduAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${baidu.ai.api-key}")
    private String apiKey;

    @Value("${baidu.ai.secret-key}")
    private String secretKey;

    private String accessToken;
    private long tokenExpireTime;

    /**
     * 获取百度 AI access_token，带缓存
     */
    private synchronized String getAccessToken() {
        if (accessToken != null && System.currentTimeMillis() < tokenExpireTime) {
            return accessToken;
        }
        URI uri = UriComponentsBuilder
                .fromUriString("https://aip.baidubce.com/oauth/2.0/token")
                .queryParam("grant_type", "client_credentials")
                .queryParam("client_id", apiKey)
                .queryParam("client_secret", secretKey)
                .build().toUri();
        try {
            String body = restTemplate.getForObject(uri, String.class);
            JsonNode root = objectMapper.readTree(body);
            if (root.has("error")) {
                throw new BusinessException(502, "百度AI鉴权失败: " + root.path("error_description").asText());
            }
            accessToken = root.get("access_token").asText();
            int expiresIn = root.get("expires_in").asInt();
            tokenExpireTime = System.currentTimeMillis() + (expiresIn - 60) * 1000L;
            return accessToken;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取百度AI token失败", e);
            throw new BusinessException(502, "获取百度AI token失败");
        }
    }

    /**
     * 调用百度通用物体和场景识别接口，返回识别到的关键词列表
     */
    public List<String> recognize(byte[] imageBytes) {
        String token = getAccessToken();
        String base64Img = Base64.getEncoder().encodeToString(imageBytes);

        String url = "https://aip.baidubce.com/rest/2.0/image-classify/v2/advanced_general?access_token=" + token;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("image", base64Img);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);

        try {
            ResponseEntity<String> resp = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = objectMapper.readTree(resp.getBody());

            if (root.has("error_code")) {
                log.warn("百度AI识别失败: {}", root);
                throw new BusinessException(502, "图像识别失败: " + root.path("error_msg").asText());
            }

            List<String> keywords = new ArrayList<>();
            JsonNode results = root.get("result");
            if (results != null && results.isArray()) {
                for (JsonNode item : results) {
                    String keyword = item.path("keyword").asText("");
                    if (!keyword.isBlank()) {
                        keywords.add(keyword);
                    }
                }
            }
            return keywords;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调用百度AI识别失败", e);
            throw new BusinessException(502, "图像识别服务异常");
        }
    }
}
