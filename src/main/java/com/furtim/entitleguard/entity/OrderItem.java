package com.furtim.entitleguard.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.hibernate.annotations.GenericGenerator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class OrderItem {
	
	@Id
	@GenericGenerator(name = "uuid2", strategy = "uuid2")
	@GeneratedValue(generator = "uuid2")
	@Column(length = 36, nullable = false, updatable = false)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "orderId")
	private Orders order;
	
	private String productId;
	
	private String productName;
	
	private String productType;
	
	private String productDesc;
	
	@ManyToOne
	@JoinColumn(name = "productManualId")
	private Documents productManual;
	
	@OneToMany
	@JoinColumn(name = "orderItemId") 
	private List<Documents> extraFiles;
	
	private String sku;
	
	private String brand;
	
	private Double price;
	
	private Double discount;
	
	private Double actualPrice;
	
	private Double quantity;
	
	private String unit;
	
	private String currency;
	
	private Double tax;
	
	private String productImageUrl;

	
}
