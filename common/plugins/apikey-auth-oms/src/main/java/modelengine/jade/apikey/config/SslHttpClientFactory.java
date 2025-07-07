/*---------------------------------------------------------------------------------------------
 *  Copyright (c) 2025 Huawei Technologies Co., Ltd. All rights reserved.
 *  This file is a part of the ModelEngine Project.
 *  Licensed under the MIT License. See License.txt in the project root for license information.
 *--------------------------------------------------------------------------------------------*/

package modelengine.jade.apikey.config;

import lombok.extern.slf4j.Slf4j;
import modelengine.fitframework.annotation.Component;
import modelengine.fitframework.annotation.Value;
import modelengine.fitframework.resource.Resource;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.BasicHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;

/**
 * 支持证书配置的 http 客户端
 *
 * @author 李智超
 * @since 2024/7/30
 */
@Slf4j
@Component
public class SslHttpClientFactory {
    @Value("${httpClient.ssl.common-password}")
    private String encryptedPwd;

    @Value("${httpClient.ssl.trust-store}")
    private Resource trustStore;

    @Value("${httpClient.ssl.key-store}")
    private Resource keyStore;

    private static SSLContext getSSLContext(Resource keyStore, String keyStorePwd, Resource trust, String trustPwd,
            String keyPwd) {
        try {
            return SSLContextBuilder.create()
                    .setSecureRandom(SecureRandom.getInstanceStrong())
                    .setProtocol("TLSv1.3")
                    .loadKeyMaterial(keyStore.url(), keyStorePwd.toCharArray(), keyPwd.toCharArray())
                    .loadTrustMaterial(trust.url(), trustPwd.toCharArray())
                    .build();
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Failed to find cert files.");
        } catch (UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException | KeyManagementException e) {
            throw new IllegalStateException("Failed to init SSLContext.", e);
        } catch (IOException e) {
            log.error("Failed to load cert file.");
            throw new IllegalStateException("Failed to init SSLContext.", e);
        } catch (Exception e) {
            throw new IllegalStateException("Unexpected error occurred during SSL context initialization.", e);
        }
    }

    private static BasicHttpClientConnectionManager getBasicConnectionManager(SSLContext sslContext) {
        SSLConnectionSocketFactory sslSocketFactory =
                new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("https", sslSocketFactory)
                .register("http", new PlainConnectionSocketFactory())
                .build();
        return new BasicHttpClientConnectionManager(socketFactoryRegistry);
    }

    /**
     * 获取支持证书的 http 客户端
     *
     * @return 支持证书的 http 客户端
     */
    public CloseableHttpClient getHttpClient() {
        BasicHttpClientConnectionManager connManager = getInnerBasicConnectionManager();
        return HttpClients.custom().setConnectionManager(connManager).build();
    }

    private BasicHttpClientConnectionManager getInnerBasicConnectionManager() {
        SSLContext sslContext =
                getSSLContext(keyStore, this.encryptedPwd, trustStore, this.encryptedPwd, this.encryptedPwd);
        return getBasicConnectionManager(sslContext);
    }
}