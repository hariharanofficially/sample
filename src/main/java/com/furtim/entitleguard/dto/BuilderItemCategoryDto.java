package com.furtim.entitleguard.dto;

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
public class BuilderItemCategoryDto {
	
	private String category;
	
    private List<BuilderItemDto> items;

}

