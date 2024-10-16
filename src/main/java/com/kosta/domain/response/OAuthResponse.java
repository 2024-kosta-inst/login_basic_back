package com.kosta.domain.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class OAuthResponse {
    private String authProvider;
    private LocalDateTime joinedAt;
}
