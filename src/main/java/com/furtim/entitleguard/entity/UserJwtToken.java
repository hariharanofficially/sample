package com.furtim.entitleguard.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Entity
public class UserJwtToken {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@Column(columnDefinition = "TEXT")
	private String jwt;
	
	@ManyToOne
	@JoinColumn(name = "customerId")
	private Customer customer;
	
	@ManyToOne
	@JoinColumn(name = "userInfoId")
	private UserInfo userInfo;


	private String type;

	private String logged;

	private LocalDateTime createdAt;

	private LocalDateTime loggedOutTime;

	@PrePersist
	public void prePersist() {
		this.createdAt = LocalDateTime.now();
	}

}
