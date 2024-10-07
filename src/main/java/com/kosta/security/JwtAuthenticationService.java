package com.kosta.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kosta.domain.response.LoginResponse;
import com.kosta.entity.User;
import com.kosta.repository.UserRepository;
import com.kosta.util.TokenUtils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {
	private final TokenUtils tokenUtils;
	private final UserRepository userRepository;
	
	void successAuthentication(HttpServletResponse response, Authentication authResult) throws IOException {
		User user = (User) authResult.getPrincipal();	
		Map<String, String> tokenMap = tokenUtils.generateToken(user);
		String accessToken = tokenMap.get("accessToken");
		String refreshToken = tokenMap.get("refreshToken");

		// 리프레시 토큰을 DB에 저장!
		user.setRefreshToken(refreshToken);
		userRepository.save(user);
		
		// 생성된 리프레시 토큰을 cookie에 담아 응답
		tokenUtils.setRefreshTokenCookie(response, refreshToken);
		
		// 생성된 액세스 토큰을 LoginResponse에 담아 응답
		LoginResponse loginResponse = LoginResponse.builder().accessToken(accessToken).build();
		tokenUtils.writeResponse(response, loginResponse);
	}
	
	void failureAuthentication(HttpServletResponse response) throws IOException {
		ObjectMapper objectMapper = new ObjectMapper();
		   
	    Map<String, String> errorResponse = new HashMap<>();
	    errorResponse.put("error", "로그인 실패");
	    errorResponse.put("message", "올바르지 않은 이메일 또는 비밀번호");
	    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
	    
	    response.setContentType("application/json");
	    response.setCharacterEncoding("UTF-8");
	    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	    
	    response.getWriter().write(jsonResponse);
	}
}
