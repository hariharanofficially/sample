package com.furtim.entitleguard.dto;

import java.time.LocalDate;
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
public class QueryDto {

	private String id;

	private String orderItemId;

	private String title;

	private String description;

	private String priorityLevel;

	private String vendorId;
	
	private String statusId;

	private LocalDate dueDate;

	private List<QueryFileMapDto> queryFileMapDto;

}
