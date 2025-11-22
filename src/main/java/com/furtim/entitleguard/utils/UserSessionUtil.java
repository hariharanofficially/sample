package com.furtim.entitleguard.utils;

import com.furtim.entitleguard.configuration.UserSession;
import com.furtim.entitleguard.dto.CustomerDto;

public class UserSessionUtil {
	
	private UserSessionUtil() {}

	public static CustomerDto getUserInfo() {
		return UserSession.getInstance().getCurrentUserOrElseThrow(RuntimeException::new);
	}
	
	public static String getUserId() {
		return getUserInfo().getId();
	}
	
	public static String getUserName() {
		return (getUserInfo().getEmail() != null && !getUserInfo().getEmail().isEmpty()
		        ? getUserInfo().getEmail()
		        : getUserInfo().getContact());
	}
	
	

}
