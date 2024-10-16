package com.kosta.service;

import java.util.List;

import com.kosta.domain.AuthEnum;
import com.kosta.domain.OAuthUserInfo;
import com.kosta.domain.request.SignUpRequest;
import com.kosta.domain.response.UserListResponse;

import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

	void signUp(SignUpRequest signUpRequest) throws Exception;

	OAuthUserInfo oAuthUser(String code, AuthEnum auth);

	String oAuthSignUpAndLogin(OAuthUserInfo oAuthUserInfo, HttpServletResponse res);

    List<UserListResponse> getUserAllInfo();
}
