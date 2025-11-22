package com.furtim.entitleguard.response;

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
public class DefaultListResponse {
	
	public Boolean success;

	public String message;

	public Object data;
	
	
	 public static DefaultListResponse ok(String message, Object data) {
	        return new DefaultListResponse(true, message, data);
	    }

	    public static DefaultListResponse ok(String message) {
	        return new DefaultListResponse(true, message, null);
	    }

	    public static DefaultListResponse error(String message) {
	        return new DefaultListResponse(false, message, null);
	    }

	    public static DefaultListResponse of(Boolean success, String message, Object data) {
	        return new DefaultListResponse(success, message, data);
	    }

}
