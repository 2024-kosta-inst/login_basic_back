package com.kosta.domain.response;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserListResponse {
    private Long id;
    private String email;
    private String name;
    private List<OAuthResponse> oAuth;
}