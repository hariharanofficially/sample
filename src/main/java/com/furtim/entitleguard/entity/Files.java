package com.furtim.entitleguard.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Setter
public class Files {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	private String name;

	private String type;

	private String fileType;

	private String filePath;

	@JsonIgnore
	private String referenceId;

	@JsonIgnore
	private Boolean isDeleted;

	@JsonIgnore
	private String updatedBy;

	@JsonIgnore
	private LocalDateTime updatedAt;

	@PrePersist
	public void prePersist() {
		this.isDeleted = true;
	}

	@PreUpdate
	public void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}

}
