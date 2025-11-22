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
public class Orders {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "customerSourceMapId")
	private CustomerSourceMap customerSourceMap;

	@ManyToOne
	@JoinColumn(name = "shopTokenId")
	private ShopToken shopToken;
	
	private String orderId;
	
	private LocalDate date;
	
	private String status;
	
	private String type;
	
	private String property;
	
	private String fulfilmentStatus;
	
	private Double quantity;
	
	private Double tax;
	
	private Double actualPrice;
	
	private Double totalPrice;
	
	@ManyToOne
	@JoinColumn(name = "addressId")
	private Address shipToAddress;
	
	@JsonIgnore
	private Boolean isActive;

	private LocalDateTime createdAt;

	@PrePersist
	public void prePersist() {
		this.isActive = true;
		this.createdAt = LocalDateTime.now();

	}

}
