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
public class BuilderCustomerDto {
	
	private String id;
	
	private String firstName;
	
	private String lastName;

	private String email;

	private String contact;

	private String builderOrganizationId;
	
	private String address;
	
	private String city;
	
	private String state;
	
	private String zip;
	
	private String projectName;
	
	private String notes;
	
	private LocalDate settlementDate;
	
	private String country;
}
