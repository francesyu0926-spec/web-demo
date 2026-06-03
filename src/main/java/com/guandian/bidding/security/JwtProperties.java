package com.guandian.bidding.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * JWT 配置（app.jwt.*）。
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private long expireMinutes = 720;
    private String header = "Authorization";
    private String prefix = "Bearer ";
}
