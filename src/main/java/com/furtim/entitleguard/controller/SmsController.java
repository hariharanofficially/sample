package com.furtim.entitleguard.controller;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RestController;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class SmsController {

	@Value("${twilio.account.sid}")
	private String ACCOUNT_SID;

	@Value("${twilio.auth.token}")
	private String AUTH_TOKEN;

	@Value("${twilio.phone.number}")
	private String FROM_PHONE;
	
	@PostConstruct
    public void init() {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

	public void sendOtp(String phone, String otp) {
		Message.creator(new PhoneNumber(phone), new PhoneNumber(FROM_PHONE), "Your OTP is: " + otp).create();
	}
	
	public void sendWelcomeMessage(String phone, String name) {
	    String message = "Hi " + name + ", welcome to EntitleGuard! 🛡️ "
	                   + "Track and protect your product warranties easily. "
	                   + "Download our app here: https://play.google.com/store/apps/details?id=com.entitleguard.app";

	    try {
	        Message.creator(
	            new PhoneNumber(phone),
	            new PhoneNumber(FROM_PHONE),
	            message
	        ).create();
	        log.info("Welcome SMS sent to {}", phone);
	    } catch (Exception e) {
	        log.error("Failed to send welcome SMS to {}: {}", phone, e.getMessage());
	    }
	}


}
