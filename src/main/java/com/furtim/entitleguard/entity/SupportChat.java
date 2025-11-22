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

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class SupportChat {

	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "orderItemId")
	private OrderItem orderItemId;
	
	private String productId;

	private String question;

	private String answer;

	private LocalDateTime answerCreatedAt;
	
	private LocalDateTime questionCreatedAt;
	
	@ManyToOne
	@JsonIgnore
	@JoinColumn(name = "customerId")
	private Customer createdBy;
	
	@JsonIgnore
	private Boolean isActive;

	@PrePersist
	public void prePersist() {
		this.isActive = true;

	}

}
