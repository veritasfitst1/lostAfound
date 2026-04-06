package com.example.server.service;

import com.example.server.exception.BusinessException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class WechatService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    public String getOpenid(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(400, "code 不能为空");
        }
        if ("your_appid".equals(appId) || "your_secret".equals(secret)
                || appId == null || appId.isBlank() || secret == null || secret.isBlank()) {
            throw new BusinessException(500, "请在 application.yml 中配置 wechat.appid 与 wechat.secret");
        }

        URI uri = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUri();

        String body;
        try {
            body = restTemplate.getForObject(uri, String.class);
        } catch (Exception e) {
            throw new BusinessException(502, "请求微信服务失败");
        }
        if (body == null || body.isBlank()) {
            throw new BusinessException(502, "微信服务无响应");
        }

        try {
            JsonNode root = objectMapper.readTree(body);
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                String msg = root.path("errmsg").asText("微信登录失败");
                throw new BusinessException(400, msg);
            }
            JsonNode openidNode = root.get("openid");
            if (openidNode == null || openidNode.asText().isBlank()) {
                throw new BusinessException(400, "未能获取微信身份");
            }
            return openidNode.asText();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(502, "解析微信响应失败");
        }
    }
}
