package com.furtim.entitleguard.dto;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.web.ProjectedPayload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class BuilderOrderManualDto {
	
	private String customerId;
	
	private String projectName;
	
    private String address;
    private String city;
    private String state;
    private String country;
    private String zip;
    
    private LocalDate settlementDate;
    
    private String notes;

    private String builderName;
    
    private List<String> catlogIds;

}
