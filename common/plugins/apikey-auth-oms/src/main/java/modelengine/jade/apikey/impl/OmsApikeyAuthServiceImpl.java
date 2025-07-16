/*
 * Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 * This file is a part of the ModelEngine Project.
 * Licensed under the MIT License. See License.txt in the project root for license information.
 */

package modelengine.jade.apikey.impl;

import com.fasterxml.jackson.databind.ObjectMapper;

import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.log.Logger;
import modelengine.jade.apikey.ApikeyAuthService;
import modelengine.jade.apikey.config.SslHttpClientFactory;
import modelengine.jade.apikey.utils.Pbkdf2Util;
import modelengine.jade.oms.response.ResultVo;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 表示北向接口 Apikey 鉴权的 Oms 实现。
 *
 * @author 陈潇文
 * @since 2025-07-07
 */
@Component
public class OmsApikeyAuthServiceImpl implements ApikeyAuthService {
    private static final Logger log = Logger.get(OmsApikeyAuthServiceImpl.class);
    private static final Pattern API_KEY_PATTERN = Pattern.compile("Bearer ME-sk-[A-Za-z0-9]{16}-[A-Za-z0-9]{32}");
    private static final String APIKEY_AUTH_BODY_TEMPLATE = "{\"encrypted_key\":\"%s\"}";

    @Value("${oms.apikey.url}")
    private String verifyApiKeyUrl;

    private final SslHttpClientFactory sslHttpClientFactory;

    /**
     * 用证书配置的 http 客户端 {@link SslHttpClientFactory} 构造 {@link OmsApikeyAuthServiceImpl}。
     *
     * @param sslHttpClientFactory 表示证书配置的 http 客户端的 {@link SslHttpClientFactory}。
     */
    public OmsApikeyAuthServiceImpl(SslHttpClientFactory sslHttpClientFactory) {
        this.sslHttpClientFactory = sslHttpClientFactory;
    }

    @Override
    public Boolean authApikeyInfo(String apikey) {
        log.info("Starting API key authentication for API key:");
        if (!isValidApiKey(apikey)) {
            log.error("API key is invalid");
            return Boolean.FALSE;
        }
        List<String> infos = Arrays.asList(apikey.split("-"));
        String salt = infos.get(2);
        String sk = infos.get(3);
        String encrypted = Pbkdf2Util.pbkdf2ForPassStandard(sk, salt);
        ResultVo queryResponse = this.buildPostRequest(salt + '-' + encrypted);
        if (queryResponse.getData().equals(Boolean.TRUE)) {
            log.info("API key authentication successful.");
            return Boolean.TRUE;
        }
        log.info("API key authentication failed.");
        return Boolean.FALSE;
    }

    private ResultVo buildPostRequest(String apikey) {
        HttpPost httpPost = new HttpPost(this.verifyApiKeyUrl);
        try (StringEntity jsonBody = new StringEntity(String.format(Locale.ROOT, APIKEY_AUTH_BODY_TEMPLATE, apikey),
                ContentType.APPLICATION_JSON); CloseableHttpClient client = this.sslHttpClientFactory.getHttpClient()) {
            httpPost.setEntity(jsonBody);
            CloseableHttpResponse response = client.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(responseBody, ResultVo.class);
        } catch (IOException e) {
            log.error("I/O exception occurred during POST request: {}", e.getMessage(), e);
            return this.createFailedResultVo();
        } catch (ParseException e) {
            log.error("Can't parse verify result. Cause: {}", e.getMessage(), e);
            return this.createFailedResultVo();
        }
    }

    private static boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("API key is null or empty.");
            return false;
        }
        return API_KEY_PATTERN.matcher(apiKey).matches();
    }

    private ResultVo<Boolean> createFailedResultVo() {
        ResultVo<Boolean> resultVo = new ResultVo<>();
        resultVo.setData(false);
        return resultVo;
    }
}
