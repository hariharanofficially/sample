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
public class VendorDto {
	
	private String id;

	private String name;
	
	private String email;
	
	private String contact;
	
	private String type;
	
	private String description;
	
	private String builderOrganizationId;

}
