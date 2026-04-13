package com.rubymusic.interaction.config;

import com.rubymusic.interaction.client.auth.api.InternalAuthApi;
import com.rubymusic.interaction.client.auth.model.ServiceTokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ServiceTokenProvider}.
 *
 * TDD cycle:
 * RED  — written before refactoring; constructor still takes AuthServiceClient → compile failure
 * GREEN — ServiceTokenProvider refactored to accept InternalAuthApi; all tests pass
 * TRIANGULATE — covers: first call fetches, second call caches, expired token refreshes
 */
class ServiceTokenProviderTest {

    private InternalAuthApi mockAuthApi;
    private ServiceTokenProvider provider;

    @BeforeEach
    void setUp() {
        mockAuthApi = mock(InternalAuthApi.class);
        // RED: constructor currently takes AuthServiceClient — this won't compile until GREEN
        provider = new ServiceTokenProvider(mockAuthApi);
        ReflectionTestUtils.setField(provider, "serviceName", "interaction-service");
        ReflectionTestUtils.setField(provider, "serviceSecret", "test-secret");
    }

    // ── Happy path ────────────────────────────────────────────────────────────

    @Test
    void getToken_firstCall_fetchesTokenFromAuthApiAndReturnsIt() {
        // Given
        ServiceTokenResponse response = new ServiceTokenResponse()
                .token("service-jwt-xyz")
                .expiresIn(3600);
        when(mockAuthApi.getServiceToken("interaction-service", "test-secret"))
                .thenReturn(response);

        // When
        String token = provider.getToken();

        // Then
        assertThat(token).isEqualTo("service-jwt-xyz");
        verify(mockAuthApi, times(1)).getServiceToken("interaction-service", "test-secret");
    }

    // TRIANGULATE: subsequent call must NOT hit auth-service again (cache hit)
    @Test
    void getToken_secondCall_returnsCachedTokenWithoutAdditionalApiCall() {
        // Given
        ServiceTokenResponse response = new ServiceTokenResponse()
                .token("cached-token")
                .expiresIn(3600);
        when(mockAuthApi.getServiceToken(anyString(), anyString())).thenReturn(response);
        provider.getToken(); // warm the cache

        // When
        String token = provider.getToken();

        // Then
        assertThat(token).isEqualTo("cached-token");
        verify(mockAuthApi, times(1)).getServiceToken(anyString(), anyString()); // only 1 call total
    }

    // TRIANGULATE: expired token (expiresIn=0) triggers a refresh on next call
    @Test
    void getToken_tokenExpired_refreshesTokenFromAuthApi() {
        // Given — first token expires immediately (0s TTL → always within 60s pre-refresh window)
        ServiceTokenResponse expiredResponse = new ServiceTokenResponse()
                .token("old-token")
                .expiresIn(0);
        ServiceTokenResponse refreshedResponse = new ServiceTokenResponse()
                .token("refreshed-token")
                .expiresIn(3600);
        when(mockAuthApi.getServiceToken(anyString(), anyString()))
                .thenReturn(expiredResponse, refreshedResponse);

        provider.getToken(); // first call — caches token that is immediately "expired"

        // When
        String token = provider.getToken(); // second call — must detect near-expiry and refresh

        // Then
        assertThat(token).isEqualTo("refreshed-token");
        verify(mockAuthApi, times(2)).getServiceToken(anyString(), anyString());
    }
}
