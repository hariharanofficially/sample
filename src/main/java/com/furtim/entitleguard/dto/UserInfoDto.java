package com.furtim.entitleguard.dto;

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
public class UserInfoDto {
	
	private String id;

	private String firstName;
	
	private String lastName;
	
	private String role;

	private String email;

	private String contact;
	
	private String builderOrganizationId;

}
