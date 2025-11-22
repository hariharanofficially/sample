package com.furtim.entitleguard.dto;

import java.time.LocalDate;

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
public class CustomerDto {
	
	private String id;
	
	private String name;
	
	private String email;
	
	private String contact;
	
	private LocalDate dob;
	
	private Boolean isRegistered;
	
	private String otp;
	
	private AddressDto addressDto;
	
	private MultipartFile file;

}
