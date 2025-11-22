package com.furtim.entitleguard.dto;

import java.time.LocalDate;

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
public class QueryDetailDto {
	
	private String id;
	
	private String title;
	
	private String statusName;
	
	private LocalDate date;

}
