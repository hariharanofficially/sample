package com.furtim.entitleguard.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.BuilderCustomerDto;
import com.furtim.entitleguard.dto.BuilderCustomerItemMapWrapper;
import com.furtim.entitleguard.dto.BuilderItemDto;
import com.furtim.entitleguard.dto.BuilderOrderManualDto;
import com.furtim.entitleguard.dto.BuilderOrganizationDto;
import com.furtim.entitleguard.dto.CustomerItemMapDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.BuilderService;
import com.furtim.entitleguard.service.OrderService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/api")
public class BuilderController {

	private final BuilderService builderService;
	
	private final OrderService orderService;
	
	@GetMapping(value = "/getcategorys")
	public DefaultListResponse getCategorys() {
		return builderService.getCategorys();
	}
	
	@GetMapping(value = "/getbillofmaterials")
	public DefaultListResponse getBillofMaterials() {
		log.info("fetching the bill materials...");
		return builderService.getBillOfMaterials();
	}
	
	@GetMapping(value = "/getbillmaterials")
	public DefaultListResponse getBillMaterials(@RequestParam(name = "billId") String billId) {
		log.info("bill Id : {}", billId);
		return builderService.getBillMaterials(billId);
	}

	@GetMapping("/builder/item")
	public DefaultListResponse getBuilderItem(@RequestParam(name = "builderId") String builderId) {
		return builderService.getBuilderItem(builderId);
	}
	
	@GetMapping("/dashboard/count")
	public DefaultListResponse getDashBoardCount(@RequestParam(name = "builderId") String builderId) {
		return builderService.getDashBoardCount(builderId);
	}
	
	@GetMapping("/dashboard/customerlist")
	public DefaultListResponse getDashBoardCustomerList(@RequestParam(name = "builderId") String builderId) {
		return builderService.getDashBoardCustomerList(builderId);
	}
		
	
	@GetMapping("/customerdetails")
	public DefaultListResponse getCustomerDetails(@RequestParam(name = "customerId") String customerId,@RequestParam(name = "builderId") String builderId) {
		return builderService.getCustomerDetails(customerId,builderId);
	}
	
	@GetMapping("/builderorganization")
	public DefaultListResponse getBuilderOrganization(@RequestParam(name = "builderId") String builderId) {
		return builderService.getBuilderOrganization(builderId);
	}
	
	@PostMapping("/builder/item")
	public ApiResponse addItems(@RequestBody BuilderItemDto itemDto) {
		log.info("itemDto {}", itemDto);
		return builderService.addItems(itemDto);
	}
	
	@PostMapping("/builder/organization")
	public ApiResponse addBuilderOrganization(@RequestBody BuilderOrganizationDto builderDto) {
		log.info("builderDto {}", builderDto);
		return builderService.addBuilderOrganization(builderDto);
	}
	
	@PostMapping("/builder/customer")
	public DefaultListResponse addCustomer(@RequestBody BuilderCustomerDto customerDto) {
		log.info("customerDto {}", customerDto);
		return builderService.addBuilderCustomer(customerDto);
	}
	
    @PostMapping("/create/customerentitlement/{builderCustomerId}")
	public ApiResponse createOrderFromBuilderCustomer(@PathVariable String builderCustomerId) {
		log.info("builderCustomerId {}", builderCustomerId);
		return orderService.createOrderFromBuilderCustomer(builderCustomerId);
	}

	@PostMapping("/builder/customeritem")
	public ApiResponse addCustomerItem(@RequestBody CustomerItemMapDto customerDto) {
		log.info("customerDto {}", customerDto);
		return builderService.addBuilderCustomerItem(customerDto);
	}
	

	@PostMapping(value = "itemmap/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse addUser(@ModelAttribute BuilderCustomerItemMapWrapper wrapper) {
	    log.info("Add builderMap {}", wrapper.getBuilderMap());
	    return builderService.addOrUpdateUser(wrapper.getBuilderMap());
	}
	
	@PostMapping("/createManualBuilderOrder")
	public ApiResponse createManualBuilderOrder(@RequestBody BuilderOrderManualDto dto) {
	    return builderService.createManualBuilderOrder(dto);
	}
	
	@DeleteMapping("/builder/customer/{id}")
	public ApiResponse deleteBuilderCustomer(@PathVariable("id") String id) {
		return builderService.deleteBuilderCustomer(id);
	}

	@DeleteMapping("/builder/item/{id}")
	public ApiResponse deleteBuilderItem(@PathVariable("id") String id) {
		return builderService.deleteBuilderItem(id);
	}
	
	@DeleteMapping("itemfile/{id}")
    public ApiResponse deleteBuilderCustomerItemFile(@PathVariable("id") String id) {
        return builderService.deleteBuilderCustomerItemFile(id);
    }

}
