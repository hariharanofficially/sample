package com.furtim.entitleguard.dto;

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
public class BuilderItemDto {

	private String id;

	private String builderOrganizationId;

	private String name;

	private String category;

	private String make;

	private String brand;

	private String model;
	
	private String note;
	
	private String price;

	private String text;

	private String documentationUrl;

	private String status;

	private String purchaser;

	private Boolean mapped;
	
	private String builderCustomerMapId;
	
	private String seller;
	
	private String serialNumber;
	
	private Integer documentCount;
	
	private List<FileResponseDto> FileResponseDto;
}
