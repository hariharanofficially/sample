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
public class BuilderItem {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "builderOrganizationId")
	private BuilderOrganization builderOrganization;
	
	@ManyToOne
	@JoinColumn(name = "billOfMaterialId")
	private BillOfMaterials billOfMaterials;

	private String name;
	
	private String category;
	
	private String make;
	
	private String brand;
	
	private String model;
	
	private String text;
	
	private String note;
	
	private String price;
	
	private String documentationUrl;
	
	private Boolean isActive;
	
	private String status;
	
	@JsonIgnore
	private LocalDateTime createdAt;
	
	private String puchaser;

	@PrePersist
	public void prePersist() {
		this.isActive = true;
		this.createdAt = LocalDateTime.now();
		this.status = "ACTIVE";
	}

}
