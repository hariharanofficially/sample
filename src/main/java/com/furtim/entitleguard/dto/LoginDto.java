package com.furtim.entitleguard.dto;

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
public class LoginDto {
	
	private String email;
	
	private String contact;
	
	private String password;
	
	private String otp;
	
	private String loginType;
	
	private String token;

}
