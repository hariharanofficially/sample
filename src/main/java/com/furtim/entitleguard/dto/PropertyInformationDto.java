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
public class PropertyInformationDto { 
	
	private String builderName;
    private String projectName;
    private String propertyAddress;
    private String city;
    private String state;
    private String postcode;
    private LocalDate settlementDate;
    private String additionalNotes;
    private String customerId;

}
