package com.qbe.springstarter.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;

@ConfigurationProperties(prefix = "app.security")
public record KeyProperties(Resource publicKey, Resource privateKey) {}
