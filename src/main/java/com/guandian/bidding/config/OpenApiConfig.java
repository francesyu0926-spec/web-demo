package com.guandian.bidding.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Knife4j / OpenAPI3 文档配置。访问地址：/doc.html
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME = "bearer-jwt";

    @Bean
    public OpenAPI biddingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("观点科技电子招投标交易平台 API")
                        .version("1.0.0")
                        .description("依据《研发需求文档 V1.4》")
                        .contact(new Contact().name("观点科技")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME))
                .components(new Components().addSecuritySchemes(SECURITY_SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
