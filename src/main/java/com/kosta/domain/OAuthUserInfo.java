package com.kosta.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OAuthUserInfo {
	String key; 
	String email;
	String name;
	AuthEnum provider;
	
	public boolean isAbleToLogin() {
		if (this.getKey() != null && this.getEmail() != null && this.getName() != null && this.getProvider() != null) {
			return true;
	    }
		return false;
	}
}