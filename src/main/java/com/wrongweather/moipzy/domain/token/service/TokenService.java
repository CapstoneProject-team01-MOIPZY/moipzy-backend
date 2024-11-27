package com.wrongweather.moipzy.domain.token.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.wrongweather.moipzy.domain.token.Token;
import com.wrongweather.moipzy.domain.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final TokenRepository tokenRepository;
    private final RestTemplate restTemplate;

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.google.token-uri}")
    private String tokenUri;

    @Transactional
    public void refreshTokens() {
        List<Token> tokens = tokenRepository.findAll(); // DB에서 모든 토큰 조회

        for (Token token : tokens) {
            try {
                // 갱신 요청
                ResponseEntity<GoogleTokenResponse> response = requestNewAccessToken(token.getRefreshToken());
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    // 토큰 갱신
                    GoogleTokenResponse tokenResponse = response.getBody();
                    token.updateAccessToken(tokenResponse.getAccessToken());
                    tokenRepository.save(token);
                }
            } catch (Exception e) {
                // 갱신 실패 시 로그
                System.err.println("Failed to refresh token for user_id: " + token.getUserId());
                e.printStackTrace();
            }
        }
    }

    private ResponseEntity<GoogleTokenResponse> requestNewAccessToken(String refreshToken) {
        // Google OAuth 토큰 요청 파라미터
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("refresh_token", refreshToken);
        params.add("grant_type", "refresh_token");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        // Google 토큰 갱신 요청
        return restTemplate.postForEntity(tokenUri, request, GoogleTokenResponse.class);
    }
}