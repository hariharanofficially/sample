package com.furtim.entitleguard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.QueryDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.QueryService;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api")
public class QueryController {
	
	private final QueryService queryService;
	
	@PostMapping(value = "/query", consumes = "multipart/form-data")
	public ApiResponse addOrUpdateQuery(@ModelAttribute QueryDto queryDto) {
	    log.info("QueryDto {}", queryDto);
	    return queryService.addOrUpdateQuery(queryDto);
	}

	
	@GetMapping("/query")
	public DefaultListResponse getQueryList() {
		return queryService.getQueryList();
	}

}
