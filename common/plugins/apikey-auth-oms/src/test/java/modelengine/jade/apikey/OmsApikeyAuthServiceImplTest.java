package modelengine.jade.apikey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;

import modelengine.jade.apikey.config.SslHttpClientFactory;
import modelengine.jade.apikey.impl.OmsApikeyAuthServiceImpl;
import modelengine.jade.apikey.utils.Pbkdf2Util;
import modelengine.jade.oms.response.ResultVo;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.lang.reflect.Field;

/**
 * 表示 {@link OmsApikeyAuthServiceImpl} 的测试类。
 *
 * @author 陈潇文
 * @since 2025-07-07
 */
public class OmsApikeyAuthServiceImplTest {
    private OmsApikeyAuthServiceImpl service;
    private SslHttpClientFactory sslHttpClientFactory;
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;

    @BeforeEach
    void setUp() throws Exception {
        service = new OmsApikeyAuthServiceImpl();
        sslHttpClientFactory = mock(SslHttpClientFactory.class);
        httpClient = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class);

        // 注入 mock 的 SslHttpClientFactory
        Field field = OmsApikeyAuthServiceImpl.class.getDeclaredField("sslHttpClientFactory");
        field.setAccessible(true);
        field.set(service, sslHttpClientFactory);

        // 注入 URL
        Field urlField = OmsApikeyAuthServiceImpl.class.getDeclaredField("verifyApiKeyUrl");
        urlField.setAccessible(true);
        urlField.set(service, "http://mock-url");
    }

    @Test
    void testAuthApikeyInfo_invalidFormat() {
        assertThat(service.authApikeyInfo("invalid-key")).isFalse();
    }

    @Test
    void testAuthApikeyInfo_validFormat_success() throws Exception {
        String apikey = "Bearer ME-sk-1234567890abcdef-abcdef1234567890abcdef1234567890";
        try (MockedStatic<Pbkdf2Util> pbkdf2Mock = mockStatic(Pbkdf2Util.class)) {
            pbkdf2Mock.when(() -> Pbkdf2Util.pbkdf2ForPassStandard(any(), any())).thenReturn("encrypted");

            when(sslHttpClientFactory.getHttpClient()).thenReturn(httpClient);
            when(httpClient.execute(any())).thenReturn(httpResponse);

            ResultVo resultVo = ResultVo.builder().data(true).build();
            StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(resultVo));
            when(httpResponse.getEntity()).thenReturn(entity);

            assertThat(service.authApikeyInfo(apikey)).isTrue();
        }
    }

    @Test
    void testAuthApikeyInfo_validFormat_fail() throws Exception {
        String apikey = "Bearer ME-sk-1234567890abcdef-abcdef1234567890abcdef1234567890";
        try (MockedStatic<Pbkdf2Util> pbkdf2Mock = mockStatic(Pbkdf2Util.class)) {
            pbkdf2Mock.when(() -> Pbkdf2Util.pbkdf2ForPassStandard(any(), any())).thenReturn("encrypted");

            when(sslHttpClientFactory.getHttpClient()).thenReturn(httpClient);
            when(httpClient.execute(any())).thenReturn(httpResponse);

            ResultVo resultVo = ResultVo.builder().data(false).build();
            StringEntity entity = new StringEntity(new ObjectMapper().writeValueAsString(resultVo));
            when(httpResponse.getEntity()).thenReturn(entity);

            assertThat(service.authApikeyInfo(apikey)).isFalse();
        }
    }

    @Test
    void testBuildPostRequest_ioException() throws Exception {
        String apikey = "Bearer ME-sk-1234567890abcdef-abcdef1234567890abcdef1234567890";
        try (MockedStatic<Pbkdf2Util> pbkdf2Mock = mockStatic(Pbkdf2Util.class)) {
            pbkdf2Mock.when(() -> Pbkdf2Util.pbkdf2ForPassStandard(any(), any())).thenReturn("encrypted");
            when(sslHttpClientFactory.getHttpClient()).thenReturn(httpClient);
            when(httpClient.execute(any())).thenThrow(new IOException("mock io error"));
            assertThat(service.authApikeyInfo(apikey)).isFalse();
        }
    }
}
