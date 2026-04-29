package com.dongmedicine.integration;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.dao.SaTokenDaoDefaultImpl;
import com.dongmedicine.mq.producer.*;
import com.dongmedicine.service.CaptchaService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Sql(scripts = "classpath:schema-test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Import(BaseIntegrationTest.MockConfig.class)
public abstract class BaseIntegrationTest {

    protected static final String TEST_USERNAME = "testuser";
    protected static final String TEST_PASSWORD = "Test1234";
    protected static final String ADMIN_USERNAME = "admin";
    protected static final String ADMIN_PASSWORD = "Admin1234";
    protected static final String TEST_CAPTCHA_KEY = "test-captcha-key";
    protected static final String TEST_CAPTCHA_CODE = "1234";

    @TestConfiguration
    static class MockConfig {
        @Bean
        @Primary
        public SaTokenDao saTokenDao() {
            return new SaTokenDaoDefaultImpl();
        }

        @Bean
        @Primary
        public RabbitTemplate rabbitTemplate() {
            return mock(RabbitTemplate.class);
        }

        @Bean
        @Primary
        public RedisConnectionFactory redisConnectionFactory() {
            RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
            RedisConnection connection = mock(RedisConnection.class);
            when(factory.getConnection()).thenReturn(connection);
            return factory;
        }

        @Bean
        @Primary
        public StatisticsProducer statisticsProducer() {
            return mock(StatisticsProducer.class);
        }

        @Bean
        @Primary
        public FileProcessProducer fileProcessProducer() {
            return mock(FileProcessProducer.class);
        }

        @Bean
        @Primary
        public FeedbackProducer feedbackProducer() {
            return mock(FeedbackProducer.class);
        }

        @Bean
        @Primary
        public OperationLogProducer operationLogProducer() {
            return mock(OperationLogProducer.class);
        }

        @Bean
        @Primary
        public NotificationProducer notificationProducer() {
            return mock(NotificationProducer.class);
        }

        @Bean
        @Primary
        public CaptchaService captchaService() {
            CaptchaService mock = mock(CaptchaService.class);
            doNothing().when(mock).validateCaptchaOrThrow(any(), any());
            when(mock.generateCaptcha()).thenReturn(
                    new CaptchaService.CaptchaResult(TEST_CAPTCHA_KEY, "data:image/png;base64,test")
            );
            return mock;
        }
    }
}
