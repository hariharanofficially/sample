package com.furtim.entitleguard.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BillMaterialDto {
	
	private String bomName;
	
	private String projectName;
	
	private MultipartFile file;
	
	private String builderOrganizationId;

}
