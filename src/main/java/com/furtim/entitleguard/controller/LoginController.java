package com.furtim.entitleguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.LoginDto;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.response.JwtResponse;
import com.furtim.entitleguard.service.BuilderService;
import com.furtim.entitleguard.service.CustomerService;
import com.furtim.entitleguard.service.MailService;
import com.furtim.entitleguard.response.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
public class LoginController {
	
	private final CustomerService customerService;
	
	private final BuilderService builderService;
	
	private final MailService mailService;
	
	@PostMapping(value = "/unsecure/basiclogin")
	public JwtResponse usersLogin (@RequestBody LoginDto loginDto) {
		log.info("Login User {}", loginDto);
		return customerService.basicLogin(loginDto);
		
	}
	
	@PostMapping(value = "/unsecure/builderlogin")
	public JwtResponse builderLogin (@RequestBody LoginDto loginDto) {
		log.info("Login User {}", loginDto);
		return builderService.builderLogin(loginDto);
		
	}
	
	@PostMapping(value = "/unsecure/sendotp")
	public JwtResponse sendOtp (@RequestBody LoginDto loginDto) {
		log.info("Login User {}", loginDto);
		return customerService.sendOtp(loginDto);
		
	}
	
	@PostMapping(value = "/unsecure/verifyotp")
	public JwtResponse otpVerify(@RequestBody LoginDto loginDto) {
		log.info("Login User {}", loginDto);
		return customerService.otpVerify(loginDto);
		
	}
	
	@GetMapping("/unsecure/presentcustomer")
	public DefaultListResponse presentCustomer( @RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "phone", required = false) String phone) {
		return customerService.presentCustomer(phone,email);
	}
	
	@GetMapping(value = "/unsecure/verify/mail")
	public ApiResponse sentVerifyMail(@RequestParam(name = "email") String email) {
		log.info("verify mail sent", email);
		return builderService.sendVerifyMail(email);

	}
	
	@PostMapping(value = "/unsecure/user/setpwd")
	public ApiResponse setPasswordForUser(@RequestBody LoginDto loginDto) {
	     log.info("Set password {}", loginDto);
	     return mailService.setPasswordForUser(loginDto);
	}

	
}
