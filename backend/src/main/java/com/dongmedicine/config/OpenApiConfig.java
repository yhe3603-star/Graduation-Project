package com.dongmedicine.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.Components;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(apiInfo())
                .servers(List.of(
                        new Server().url("http://localhost:" + serverPort).description("开发服务器"),
                        new Server().url("http://47.112.111.115").description("生产服务器")
                ))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT认证，格式: Bearer {token}")
                        )
                );
    }

    private Info apiInfo() {
        return new Info()
                .title("侗乡医药数字展示平台 API")
                .version("1.0.0")
                .description("""
                        ## 侗乡医药数字展示平台后端API文档
                        
                        ### 认证说明
                        大部分接口需要JWT认证，请在请求头中添加：
                        ```
                        Authorization: Bearer {your_token}
                        ```
                        
                        ### 接口分类
                        - **用户模块**: 用户登录、注册、信息管理
                        - **知识模块**: 侗族医药知识管理
                        - **传承人模块**: 传承人信息管理
                        - **植物模块**: 药用植物信息管理
                        - **资源模块**: 多媒体资源管理
                        - **问答模块**: 知识问答管理
                        
                        ### 错误码说明
                        - 200: 成功
                        - 400: 请求参数错误
                        - 401: 未授权/Token过期
                        - 403: 权限不足
                        - 404: 资源不存在
                        - 500: 服务器内部错误
                        """)
                .contact(new Contact()
                        .name("开发团队")
                        .email("support@dongmedicine.com"))
                .license(new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT"));
    }
}
