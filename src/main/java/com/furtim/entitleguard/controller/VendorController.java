package com.furtim.entitleguard.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.VendorDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.VendorService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api")
public class VendorController {
	
	private final VendorService vendorService;
	
	@PostMapping("/builder/vendor")
	public ApiResponse addVendor(@RequestBody VendorDto vendorDto) {
		log.info("vendorDto {}", vendorDto);
		return vendorService.addVendor(vendorDto);
	}

	@GetMapping("/builder/vendor")
	public DefaultListResponse getBuilderVendor(@RequestParam(name = "builderId") String builderId) {
		return vendorService.getBuilderVendor(builderId);
	}
	
	@DeleteMapping("/builder/vendor/{id}")
	public ApiResponse deleteBuilderVendor(@PathVariable("id") String id) {
		return vendorService.deleteBuilderVendor(id);
	}

}
