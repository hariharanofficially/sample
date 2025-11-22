package com.furtim.entitleguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.CustomerDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.CustomerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
public class CustomerController {

	private final CustomerService customerService;

	@PatchMapping("/api/customer")
	public ApiResponse updateCustomer(@RequestBody CustomerDto customerDto) {
		log.info("Update Customer {}", customerDto);
		return customerService.updateCustomer(customerDto);
	}
	
	@GetMapping("/unsecure/validtoken")
	public DefaultListResponse checkValidToken(@RequestParam (name = "token") String token) {;
		return customerService.checkValidToken(token);
	}

}
