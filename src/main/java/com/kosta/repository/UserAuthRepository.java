package com.kosta.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kosta.domain.AuthEnum;
import com.kosta.entity.UserAuth;

@Repository
public interface UserAuthRepository extends JpaRepository<UserAuth, Long>{

	Optional<UserAuth> findByAuthKeyAndProvider(String key, AuthEnum provider);
	
}
