package com.kosta.domain;

public enum AuthEnum {
	KAKAO("kakao"),
	GOOGLE("google");
	
	String auth;
	
	AuthEnum(String auth) {
		this.auth = auth;
	}
	
	public String getAuth() {
		return auth;
	}
	
	public static AuthEnum fromString(String auth) {
        for (AuthEnum authEnum : AuthEnum.values()) {
            if (authEnum.getAuth().equalsIgnoreCase(auth)) {
                return authEnum;
            }
        }
        throw new IllegalArgumentException("없는 값: " + auth);
    }
}
