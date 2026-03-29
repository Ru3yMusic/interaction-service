package com.rubymusic.interaction.dto;

/**
 * Response from auth-service POST /api/v1/auth/internal/service-token.
 */
public record ServiceTokenResponse(String token, long expiresIn) {}
