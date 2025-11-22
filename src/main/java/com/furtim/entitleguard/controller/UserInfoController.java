package com.furtim.entitleguard.controller;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.UserInfoDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.UserInfoService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/api")
public class UserInfoController {
	
	private final UserInfoService userInfoService;
	
	@PostMapping("/builder/user")
	public ApiResponse addUserInfo(@RequestBody UserInfoDto userInfoDto) {
		log.info("userinfo {}",userInfoDto);
		return  userInfoService.addOrUpdateUserInfo(userInfoDto);
	}
	
	@GetMapping("/builder/user")
	public DefaultListResponse getBuilderUser(@RequestParam(name = "builderId") String builderId) {
		return userInfoService.getBuilderUser(builderId);
	}
	
	@DeleteMapping(value = "builder/user/{id}")
	public ApiResponse deleteUser(@PathVariable(name = "id") String id) {
		log.info("Id {}", id);
		return userInfoService.deleteUser(id);
	}

}
