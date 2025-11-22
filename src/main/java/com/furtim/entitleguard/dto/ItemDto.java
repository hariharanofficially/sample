package com.furtim.entitleguard.dto;

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
public class ItemDto {
	
	private String orderItemId;
	
	private String itemName;
	
	private String brand;
	
	private String itemImage;
	
}
