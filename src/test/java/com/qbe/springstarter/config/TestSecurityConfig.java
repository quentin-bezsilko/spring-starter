package com.qbe.springstarter.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class TestSecurityConfig {

    private static final String USERNAME = "admin";
    private static final String ACCESS_TOKEN = "access-token";

    private static final Instant ISSUED_AT = Instant.parse("2026-09-25T06:00:00Z");

    private static final Instant EXPIRES_AT = ISSUED_AT.plusSeconds(3600);

    private final SecurityConfig securityConfig = new SecurityConfig();

    @Test
    void shouldCreateJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        assertThat(converter).isNotNull();
    }

    @Test
    void shouldMapAuthoritiesClaim() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwt(List.of("READ", "WRITE"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("READ", "WRITE");
    }

    @Test
    void shouldMapReadAuthority() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwt(List.of("READ"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("READ");
    }

    @Test
    void shouldMapWriteAuthority() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwt(List.of("WRITE"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("WRITE");
    }

    @Test
    void shouldNotPrefixAuthorities() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwt(List.of("READ"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("READ")
                .doesNotContain("SCOPE_READ", "ROLE_READ");
    }

    @Test
    void shouldReturnNoBusinessAuthoritiesWhenClaimIsMissing() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwtWithoutAuthorities();

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .doesNotContain("READ", "WRITE", "SCOPE_READ", "SCOPE_WRITE");
    }

    @Test
    void shouldUseSubjectAsPrincipalName() {
        JwtAuthenticationConverter converter = securityConfig.jwtAuthenticationConverter();

        Jwt jwt = createJwt(List.of("READ"));

        AbstractAuthenticationToken authentication = converter.convert(jwt);

        assertThat(authentication).isNotNull();

        assertThat(authentication.getName()).isEqualTo(USERNAME);
    }

    private Jwt createJwt(List<String> authorities) {
        return new Jwt(
                ACCESS_TOKEN,
                ISSUED_AT,
                EXPIRES_AT,
                Map.of(
                        "alg", "RS256",
                        "typ", "JWT"),
                Map.of(
                        "sub", USERNAME,
                        "userId", 2L,
                        "authorities", authorities));
    }

    private Jwt createJwtWithoutAuthorities() {
        return new Jwt(
                ACCESS_TOKEN,
                ISSUED_AT,
                EXPIRES_AT,
                Map.of(
                        "alg", "RS256",
                        "typ", "JWT"),
                Map.of("sub", USERNAME, "userId", 2L));
    }
}
