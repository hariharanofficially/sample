package com.furtim.entitleguard.dto;

import java.time.LocalDate;

import com.furtim.entitleguard.entity.Address;

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
public class OrderDto {
	
	private String id;
	
	private String orderId;
	
	private LocalDate orderDate;
	
	private Integer noOfItem;
	
	private String source;
	
	private Address address;
	
}
