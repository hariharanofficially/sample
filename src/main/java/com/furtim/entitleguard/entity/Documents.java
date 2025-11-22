package com.furtim.entitleguard.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.GenericGenerator;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Documents {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	private String type;
	
	private Double size;
	
	private String documentSource;
	
	private String location;
	
	private String contentType;
	
	@JsonIgnore
	@ManyToOne
	@JoinColumn(name = "orderItemId")
	private OrderItem orderItem;

}
