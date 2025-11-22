package com.furtim.entitleguard.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.furtim.entitleguard.dto.AddPropertyItemsRequest;
import com.furtim.entitleguard.dto.PropertyInformationDto;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.OrderService;
import com.furtim.entitleguard.service.ProductService;
import com.furtim.entitleguard.service.SearchService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class OrderController {

	private final OrderService orderService;

	private final ProductService productService;
	
	private final SearchService searchService;

	@GetMapping("/expiry/entitlementproduct/bycustomer")
	public DefaultListResponse getProduct() {
		return orderService.getProductByCustomer();
	}

	@GetMapping("/warrenty/product/bycustomer")
	public DefaultListResponse getWarrentyProduct() {
		return orderService.getWarrentyProduct();
	}

	@GetMapping("/catlog")
	public DefaultListResponse getCatlog() {
		return orderService.getCatlog();
	}

	@GetMapping("/allproduct/bycustomer")
	public DefaultListResponse getAllProduct() {
		return orderService.getAllProductByCustomer();
	}

	@GetMapping("/allorder/bycustomer")
	public DefaultListResponse getAllOrders() {
		return orderService.getAllOrdersByCustomer();
	}
	
	@GetMapping("/allorder/bycustomer/type")
	public DefaultListResponse getAllTypeOrders(@RequestParam(name = "type") String type) {
		return orderService.getAllTypeOrders(type);
	}

	@GetMapping("/product/byproductcategory/customer")
	public DefaultListResponse getproductByProductCategoryAndCustomer(
			@RequestParam(name = "productType") String productType) {
		return orderService.getproductByProductCategoryAndCustomer(productType);
	}

	@GetMapping("/productdetail/byid")
	public DefaultListResponse getProductDetails(@RequestParam(name = "orderItemId") String orderItemId) {
		return orderService.getProductDetails(orderItemId);
	}

	@GetMapping("/orderdetails/byorderid")
	public DefaultListResponse getOrderDetails(@RequestParam(name = "orderId") String orderId) {
		return orderService.getOrderDetails(orderId);
	}
	
	@GetMapping("/orderdetails/byid")
	public DefaultListResponse getOrderDetailsById(@RequestParam(name = "id") String id) {
		return orderService.getOrderDetailsById(id);
	}

	@GetMapping("/productcategory/bycustomer")
	public DefaultListResponse getAllProductCategory() {
		return productService.getAllProductCategory();
	}

	@GetMapping("/getproduct/bysearch")
	public DefaultListResponse getProductBySearch(@RequestParam(name = "search") String search,
			@RequestParam(name = "productType", required = false) String productType) {
		return orderService.getProductBySearch(search, productType);
	}

	@GetMapping("/getproductCategories/bysearch")
	public DefaultListResponse getProductCategoriesBySearch(@RequestParam(name = "search") String search) {
		return orderService.getProductCategoriesBySearch(search);
	}

	@PostMapping(value = "/upload/receipt", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public DefaultListResponse uploadReceipt(
	        @RequestParam("file") MultipartFile file,
	        @RequestParam("customerId") String customerId) {
	    return searchService.uploadReceipt(file, customerId);
	}
	
//	@PostMapping(value = "/add/propertyitem")
//	public ApiResponse addPropertyItems(
//			 @RequestParam String orderId,
//		        @RequestParam List<String> catalogIds) {
//	    return orderService.addPropertyItems(orderId, catalogIds);
//	}
	@PostMapping(value = "/add/propertyitem")
	public ApiResponse addPropertyItems(@RequestBody AddPropertyItemsRequest request) {
	    return orderService.addPropertyItems(request.getOrderId(), request.getCatalogIds());
	}

	
	@PostMapping("/upload/manualproperty")
	public DefaultListResponse uploadManualProperty(@RequestBody PropertyInformationDto propertyDto ) {
		return orderService.uploadManualProperty(propertyDto);
	}

	@DeleteMapping(value = "/delete/order/{id}")
	public ApiResponse deleteOrder(@PathVariable(name = "orderid") String orderid) {
		log.info("Delete Order id {}", orderid);
		return orderService.deleteOrderById(orderid);
	}

}
