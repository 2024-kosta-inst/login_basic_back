package com.kosta.service;

import com.kosta.domain.AuthEnum;
import com.kosta.domain.OAuthUserInfo;
import com.kosta.domain.request.SignUpRequest;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

	void signUp(SignUpRequest signUpRequest) throws Exception;

	OAuthUserInfo oAuthUser(String code, AuthEnum auth);

	String oAuthSignUpAndLogin(OAuthUserInfo oAuthUserInfo, HttpServletResponse res);
}
