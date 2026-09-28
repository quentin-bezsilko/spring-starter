package com.qbe.springstarter.config;

import com.qbe.springstarter.properties.JwtProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfig {

    private static final String RSA = "RSA";

    private static final String PUBLIC_KEY_BEGIN = "-----BEGIN PUBLIC KEY-----";

    private static final String PUBLIC_KEY_END = "-----END PUBLIC KEY-----";

    @Bean
    RSAPublicKey publicKey(JwtProperties properties) {
        try {
            String pem = properties.publicKey().getContentAsString(StandardCharsets.UTF_8);
            String encoded = pem.replace(PUBLIC_KEY_BEGIN, "").replace(PUBLIC_KEY_END, "");
            byte[] decoded = Base64.getMimeDecoder().decode(encoded);
            return (RSAPublicKey) KeyFactory.getInstance(RSA).generatePublic(new X509EncodedKeySpec(decoded));
        } catch (IOException
                | NoSuchAlgorithmException
                | InvalidKeySpecException
                | IllegalArgumentException exception) {
            throw new IllegalStateException("Unable to load RSA public key", exception);
        }
    }

    @Bean
    JwtDecoder jwtDecoder(RSAPublicKey publicKey, JwtProperties properties) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(publicKey).build();
        OAuth2TokenValidator<Jwt> defaultValidator = JwtValidators.createDefaultWithIssuer(properties.issuer());
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
                JwtClaimNames.AUD, audience -> audience.contains(properties.audience()));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(defaultValidator, audienceValidator));
        return decoder;
    }
}
