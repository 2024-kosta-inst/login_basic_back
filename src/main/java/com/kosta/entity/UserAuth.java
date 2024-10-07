package com.kosta.entity;

import com.kosta.domain.AuthEnum;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data // toString, getter, setter, equals, hashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuth {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(updatable = false)
	private Long id;
	
	@JoinColumn(name = "user_id", nullable = false)
	@ManyToOne
	private User user;
	
	@Column(nullable = true)
	@Enumerated(EnumType.STRING)
	private AuthEnum provider;
	
	@Column(nullable = true)
	private String authKey;
}
