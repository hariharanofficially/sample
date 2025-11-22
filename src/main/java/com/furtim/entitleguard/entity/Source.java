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
public class Source {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	private String name;
	
	private String code;
	
	private String email;
	
	private String contact;
	
	
	
	@ManyToOne
	@JoinColumn(name = "addressId")
	private Address address;
	
	@JsonIgnore
	private Boolean isActive;

	@JsonIgnore
	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
	}

	
	
	
	
	

}
