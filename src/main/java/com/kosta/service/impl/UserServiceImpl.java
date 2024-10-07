package com.kosta.service.impl;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.kosta.domain.AuthEnum;
import com.kosta.domain.OAuthUserInfo;
import com.kosta.domain.request.SignUpRequest;
import com.kosta.entity.User;
import com.kosta.entity.UserAuth;
import com.kosta.repository.UserAuthRepository;
import com.kosta.repository.UserRepository;
import com.kosta.service.UserService;
import com.kosta.util.OAuth2Properties;
import com.kosta.util.TokenUtils;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
	private final UserRepository userRepository;
	private final UserAuthRepository userAuthRepository;
	private final BCryptPasswordEncoder bCryptPasswordEncoder;
	private final OAuth2Properties oAuth2Properties;
	private final TokenUtils tokenUtils;
	
	@Override
	public void signUp(SignUpRequest signUpRequest) throws Exception {
		String encodedPassword = bCryptPasswordEncoder.encode(signUpRequest.getPassword());
		User user = User.builder()
				.email(signUpRequest.getEmail())
				.name(signUpRequest.getName())
				.password(encodedPassword)
				.build();
		userRepository.save(user);
	}
	
	@Override
	public OAuthUserInfo oAuthUser(String code, AuthEnum provider) {
		// 1. code를 통해 provider에서 제공하는 accessToken 가져온다.
		String providedAccessToken = getAccessToken(code, provider);
		// 2. provider에서 제공하는 accessToken으로 oAuthUserInfo를 추출한다.
		JsonNode oAuthUserNode = generateOAuthUserNode(providedAccessToken, provider);
		OAuthUserInfo oAuthUserInfo = getOAuthUserInfo(oAuthUserNode, provider);
		return oAuthUserInfo;
	}

	@Override
	@Transactional
	public String oAuthSignUpAndLogin(OAuthUserInfo oAuthUserInfo, HttpServletResponse res) {
		Optional<User> existingUser = userRepository.findByEmail(oAuthUserInfo.getEmail());
		Optional<UserAuth> existingUserAuth = userAuthRepository.findByAuthKeyAndProvider(oAuthUserInfo.getKey(), oAuthUserInfo.getProvider());
		
		User user = existingUser.orElse(null);
		if (user == null) {
	        User newUser = new User();
	        newUser.setEmail(oAuthUserInfo.getEmail());
	        newUser.setName(oAuthUserInfo.getName());
	        user = userRepository.save(newUser);
	    }
		// userAuth에 미존재 시, userAuthRepository에 userAuth 추가
		if (!existingUserAuth.isPresent()) {
	        UserAuth newUserAuth = new UserAuth();
	        newUserAuth.setAuthKey(oAuthUserInfo.getKey());
	        newUserAuth.setProvider(oAuthUserInfo.getProvider());
	        newUserAuth.setUser(user);
	        userAuthRepository.save(newUserAuth);
	    }
		
		Map<String, String> tokenMap = tokenUtils.generateToken(user);

		// DB에 기록(refresh)
		user.setRefreshToken(tokenMap.get("refreshToken"));
		userRepository.save(user);
		// HEADER에 추가(refresh)
		tokenUtils.setRefreshTokenCookie(res, tokenMap.get("refreshToken"));
		// BODY에 추가(access)
		return tokenMap.get("accessToken");
	}
	
	private OAuthUserInfo getOAuthUserInfo(JsonNode oAuthUserNode, AuthEnum provider) {
		String key = null;
		String email = null;
		String name = null;
		if (provider.equals(AuthEnum.KAKAO)) {
			key = oAuthUserNode.get("id").asText();
			name = oAuthUserNode.get("properties").get("nickname").asText();
		} else if (provider.equals(AuthEnum.GOOGLE)) {
			key = oAuthUserNode.get("sub").asText();
			email = oAuthUserNode.get("email").asText();
			name = oAuthUserNode.get("name").asText();
		}
		Optional<UserAuth> userAuth = userAuthRepository.findByAuthKeyAndProvider(key, provider);
		if (userAuth.isPresent()) {
			email = userAuth.get().getUser().getEmail();
			name = userAuth.get().getUser().getName();
		}
		return new OAuthUserInfo(key, email, name, provider);
	}

	private String getAccessToken(String code, AuthEnum provider) {
		// 설정 가져오기
		OAuth2Properties.Client client = oAuth2Properties.getClients().get(provider.getAuth());
		
		log.info(client.getClientId());
		log.info(client.getClientSecret());
		log.info(client.getRedirectUri());

		// 1. code를 통해 google에서 제공하는 accessToken 가져온다.
		String decodedCode = URLDecoder.decode(code, StandardCharsets.UTF_8);
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		headers.setBasicAuth(client.getClientId(), client.getClientSecret());
		
		MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
		params.add("client_id", client.getClientId());
		params.add("client_secret", client.getClientSecret());
		params.add("code", decodedCode);
		params.add("grant_type", "authorization_code");
		params.add("redirect_uri", client.getRedirectUri());
		
		RestTemplate rt = new RestTemplate();
		HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);
		ResponseEntity<Map> responseEntity = rt.postForEntity(client.getTokenUri(), requestEntity, Map.class);
		
		if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 가져올 수 없음");
		}
		
		return (String) responseEntity.getBody().get("access_token");
	}
	
	private JsonNode generateOAuthUserNode(String accessToken, AuthEnum provider) {
		// 설정 가져오기
		OAuth2Properties.Client client = oAuth2Properties.getClients().get(provider.getAuth());
		
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + accessToken);
		
		RestTemplate rt = new RestTemplate();
		ResponseEntity<JsonNode> responseEntity = rt.exchange(client.getUserInfoRequestUri(), HttpMethod.GET, new HttpEntity<>(headers), JsonNode.class);
		
		if (!responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사용자 정보를 가져올 수 없음");
		}
		
		JsonNode jsonNode = responseEntity.getBody();
		return jsonNode;
	}
}
