package com.furtim.entitleguard.entity;

import java.time.LocalDate;
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
public class BuilderCustomer {

	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;

	@ManyToOne
	@JoinColumn(name = "builderOrganizationId")
	private BuilderOrganization builderOrganization;
	
	@ManyToOne
	@JoinColumn(name = "statusId")
	private Status status;

	private String firstName;

	private String lastName;

	private String email;

	private String contact;

	private String address;

	private String city;

	private String state;
	
	private String country;

	private String zip;

	private String projectName;

	private String notes;

	@JsonIgnore
	private Boolean isActive;

	@JsonIgnore
	private LocalDateTime createdAt;
	
	private LocalDate settlementDate;

	@PrePersist
	public void prePersist() {
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
	}

}
