package com.rubymusic.interaction.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link JwtTokenProvider}.
 *
 * TDD cycle:
 * RED  — written before JwtTokenProvider existed (compilation failure = failing test)
 * GREEN — JwtTokenProvider created; all tests pass
 * TRIANGULATE — covers USER role, SERVICE role, invalid signature, null, blank
 */
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private KeyPair keyPair;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        keyPair = gen.generateKeyPair();
        jwtTokenProvider = new JwtTokenProvider(keyPair.getPublic());
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void parseAndValidate_validUserToken_returnsClaimsWithSubjectAndRole() {
        String token = Jwts.builder()
                .subject("user-abc123")
                .claim("role", "USER")
                .signWith(keyPair.getPrivate())
                .compact();

        Claims claims = jwtTokenProvider.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("user-abc123");
        assertThat(claims.get("role", String.class)).isEqualTo("USER");
    }

    // TRIANGULATE: SERVICE role token also parsed correctly
    @Test
    void parseAndValidate_validServiceToken_returnsClaimsWithServiceRole() {
        String token = Jwts.builder()
                .subject("catalog-service")
                .claim("role", "SERVICE")
                .signWith(keyPair.getPrivate())
                .compact();

        Claims claims = jwtTokenProvider.parseAndValidate(token);

        assertThat(claims.getSubject()).isEqualTo("catalog-service");
        assertThat(claims.get("role", String.class)).isEqualTo("SERVICE");
    }

    // ── Error cases ───────────────────────────────────────────────────────────

    // TRIANGULATE: wrong signature rejects the token
    @Test
    void parseAndValidate_tokenSignedWithDifferentKey_throwsJwtException() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair otherKeyPair = gen.generateKeyPair();

        String tokenSignedWithOtherKey = Jwts.builder()
                .subject("attacker")
                .signWith(otherKeyPair.getPrivate())
                .compact();

        assertThatThrownBy(() -> jwtTokenProvider.parseAndValidate(tokenSignedWithOtherKey))
                .isInstanceOf(JwtException.class);
    }

    @Test
    void parseAndValidate_nullToken_throwsMalformedJwtException() {
        assertThatThrownBy(() -> jwtTokenProvider.parseAndValidate(null))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void parseAndValidate_blankToken_throwsMalformedJwtException() {
        assertThatThrownBy(() -> jwtTokenProvider.parseAndValidate("   "))
                .isInstanceOf(MalformedJwtException.class);
    }

    @Test
    void parseAndValidate_malformedToken_throwsJwtException() {
        assertThatThrownBy(() -> jwtTokenProvider.parseAndValidate("not.a.jwt"))
                .isInstanceOf(JwtException.class);
    }
}
