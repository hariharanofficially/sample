package com.furtim.entitleguard.dto;

import java.time.LocalDate;

import com.furtim.entitleguard.entity.Orders;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductDetailsDto {
	
	private String Id;
	
	private String orderId;
	
	private String vendor;
	
	private String sku;
	
	private String productName;
	
	private String productImage;
	
	private String productType;
	
	private String productDesc;
	
	private String source;
	
	private String warrentyStatus;
	
	private String warrantyDays;
	
	private LocalDate warrantyExpiryDate;
	
	private Integer warrantyExpiryDays;
	
	private String warrantyFile;
	
	private String returnStatus;
	
	private String returnDay;
	
	private Integer returnExpiryDays;
	
	private String returnPolicyFile;
	
	private LocalDate returnExpiryDate;
	
	private LocalDate orderDate;
	
	private CustomerDto customerDto;


}
