package com.qbe.springstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProperties(Resource publicKey, String issuer, String audience) {}
