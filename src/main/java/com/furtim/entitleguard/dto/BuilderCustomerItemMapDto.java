package com.furtim.entitleguard.dto;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BuilderCustomerItemMapDto {

	private String id;

	private String seller;

	private String serialNumber;
	
	private List<MultipartFile> files;

}
