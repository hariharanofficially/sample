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
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class QueryFileMapDto {
	
	private String id;
	
	private String queryId;
	
	private String type;
	
	private MultipartFile files;

}
