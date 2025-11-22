package com.furtim.entitleguard.dto;

import java.util.List;

import com.furtim.entitleguard.entity.BuilderCustomer;

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
public class CustomerDetailDto {
	
	private BuilderCustomer customer;
	
	private Integer totalItems;
	
	private Integer totalCategories;
	
	private Integer mappedItems;
	
	private Integer completionPercent;
	
	private Integer totalDocuments;
	
	private List<BuilderItemCategoryDto> dtos;


}
