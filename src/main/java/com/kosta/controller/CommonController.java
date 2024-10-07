package com.kosta.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kosta.domain.AuthEnum;
import com.kosta.domain.OAuthUserInfo;
import com.kosta.domain.request.SignUpRequest;
import com.kosta.domain.response.LoginResponse;
import com.kosta.service.UserService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CommonController {
	private final UserService userService;
	
	@PostMapping("/auth/signup")
	public ResponseEntity<?> signUp(@RequestBody SignUpRequest signUpRequest) {
		log.info("[signUp] 회원가입 진행. 요청정보 : {}", signUpRequest);
		try {
			userService.signUp(signUpRequest);
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
		return ResponseEntity.status(HttpStatus.CREATED).build();
	}
	
	@GetMapping("/oauth/{provider}")
	public ResponseEntity<?> oAuthUserCheck(@RequestParam("code") final String code, @PathVariable("provider") final String provider, HttpServletResponse res) {
		log.info("들어온 코드 값 > {}, {}", code, provider);
		AuthEnum auth = AuthEnum.fromString(provider);
		log.info(auth.toString());
		OAuthUserInfo oAuthUserInfo = userService.oAuthUser(code, auth);
		if (oAuthUserInfo.isAbleToLogin()) {
			return oAuthLogin(oAuthUserInfo, res);
		}
		return ResponseEntity.ok(oAuthUserInfo);
	}
	
	@PostMapping("/oauth/login")
	public ResponseEntity<?> oAuthLogin(@RequestBody OAuthUserInfo oAuthUserInfo, HttpServletResponse res) {
		log.info(oAuthUserInfo.toString());
		try {
			if (oAuthUserInfo.isAbleToLogin()) {
				String accessToken = userService.oAuthSignUpAndLogin(oAuthUserInfo, res);
				LoginResponse loginResponse = LoginResponse
						.builder()
						.accessToken(accessToken)
						.build();
				return ResponseEntity.ok(loginResponse);				
			} else {
				throw new Exception();
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}
	}
}
