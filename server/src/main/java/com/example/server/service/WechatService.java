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

    private final RestTemplate restTemplate; //给微信服务器发送http请求
    private final ObjectMapper objectMapper; //解析json

    @Value("${wechat.appid}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;


    //将前端传来的code 转成 数据库对应的 openid
    public String getOpenid(String code) {
        if (code == null || code.isBlank()) {
            throw new BusinessException(400, "code 不能为空");
        }
        //构造微信官方请求url
        URI uri = UriComponentsBuilder.fromUriString("https://api.weixin.qq.com/sns/jscode2session")
                .queryParam("appid", appId)
                .queryParam("secret", secret)
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUri();

        String body;  //接受返回的json

        try {
            body = restTemplate.getForObject(uri, String.class);  //发送请回，接收json
        } catch (Exception e) {
            throw new BusinessException(502, "请求微信服务失败");
        }
        if (body == null || body.isBlank()) {
            throw new BusinessException(502, "微信服务无响应");
        }

        try {
            JsonNode root = objectMapper.readTree(body);  //解析json
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {  //失败json会包含errcode，报错
                String msg = root.path("errmsg").asText("微信登录失败");
                throw new BusinessException(400, msg);
            }
            JsonNode openidNode = root.get("openid");    //成功则获取openid
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
