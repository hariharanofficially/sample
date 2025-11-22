package com.furtim.entitleguard.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
public class OrderDetailDto {
	
	private String id;
	
	private String orderId;
	
	private String storeName;
	
	private String propertyName;
	
	private LocalDate orderDate;
	
	private String orderStatus;
	
	private String fulfilmentStatus;
	
	private CustomerDto customerDto;
	
	private List<ItemDto> items = new ArrayList<>();
	
	

}
