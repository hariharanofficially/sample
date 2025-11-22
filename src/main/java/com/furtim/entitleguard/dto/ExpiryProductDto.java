package com.furtim.entitleguard.dto;

import java.time.LocalDate;

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
public class ExpiryProductDto {
	
	private String Id;
	
	private String orderId;
	
	private String builderOrderId;
	
	private LocalDate pruchaseDate;
	
	private String orderItemName;
	
	private Integer expiryDays;
	
	private String productImage;
	
	private String source;
	
	private String entitlementType;

}
