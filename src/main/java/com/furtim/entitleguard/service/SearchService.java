package com.furtim.entitleguard.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.furtim.entitleguard.dto.ExpiryProductDto;
import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Orders;
import com.furtim.entitleguard.entity.Source;
import com.furtim.entitleguard.entity.SupportChat;
import com.furtim.entitleguard.repository.CustomerRepository;
import com.furtim.entitleguard.repository.CustomerSourceMapRepository;
import com.furtim.entitleguard.repository.FilesRepository;
import com.furtim.entitleguard.repository.OrderItemRepository;
import com.furtim.entitleguard.repository.OrderRepository;
import com.furtim.entitleguard.repository.SourceRepository;
import com.furtim.entitleguard.repository.SupportChatRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.utils.UserSessionUtil;

import io.jsonwebtoken.io.IOException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SearchService {

	private final SupportChatRepository supportChatRepo;

	private final OrderItemRepository orderItemRepo;

	private final OrderRepository orderRepo;

	private final CustomerSourceMapRepository customerSourceMapRepo;

	private final SourceRepository sourceRepo;

	private final CustomerRepository customerRepo;

	private final FilesRepository filesRepo;

	public SearchService(SupportChatRepository supportChatRepo, OrderItemRepository orderItemRepo,
			OrderRepository orderRepo, CustomerSourceMapRepository customerSourceMapRepo, SourceRepository sourceRepo,
			CustomerRepository customerRepo, FilesRepository filesRepo) {
		this.supportChatRepo = supportChatRepo;
		this.orderItemRepo = orderItemRepo;
		this.customerRepo = customerRepo;
		this.customerSourceMapRepo = customerSourceMapRepo;
		this.filesRepo = filesRepo;
		this.orderRepo = orderRepo;
		this.sourceRepo = sourceRepo;
	}

	@Value("${python.api.url}")
	private String pythonApiUrl;

	@Value("${file.uploadDir}")
	private String uploadDir;

	public DefaultListResponse getSupportResponse(String orderItemId, String userInput) {

		Map<String, Object> pythonData = new HashMap<>();
		pythonData.put("order_item_id", orderItemId);
		pythonData.put("user_input", userInput);

		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(pythonData, headers);

		try {
			ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);

			String answer = "";
			if (response.getBody() != null && response.getBody().get("llm_response_openai") != null) {
				answer = response.getBody().get("llm_response_openai").toString();
			}

			Optional<OrderItem> optionalOrderItem = orderItemRepo.findById(orderItemId);
			if (!optionalOrderItem.isPresent()) {
				return new DefaultListResponse(false, "OrderItem not found", null);
			}

			SupportChat supportChat = new SupportChat();
			supportChat.setOrderItemId(optionalOrderItem.get());
			supportChat.setProductId(optionalOrderItem.get().getProductId());
			supportChat.setQuestion(userInput);
			supportChat.setAnswer(answer);
			supportChat.setQuestionCreatedAt(LocalDateTime.now());
			supportChat.setAnswerCreatedAt(LocalDateTime.now());
			supportChat.setCreatedBy(optionalOrderItem.get().getOrder().getCustomerSourceMap().getCustomer());

			supportChatRepo.save(supportChat);

			return new DefaultListResponse(true, "Python API response received", response.getBody());
		} catch (Exception ex) {
			ex.printStackTrace();
			return new DefaultListResponse(false, "Error calling Python API: " + ex.getMessage(), null);
		}
	}

	public DefaultListResponse getChatSupportHis(String orderItemId) {
		try {
			Optional<OrderItem> optionalOrderItem = orderItemRepo.findById(orderItemId);
			if (!optionalOrderItem.isPresent()) {
				return new DefaultListResponse(false, "OrderItem not found", null);
			}
			List<SupportChat> chatList = supportChatRepo.findAllByOrderItemId(orderItemId);
			return new DefaultListResponse(true, "Support chat history for Item", chatList);

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving support chat history", null);
		}
	}

	public DefaultListResponse chatSupportItemHistory() {
		try {
			String user = UserSessionUtil.getUserInfo().getId();
			List<SupportChat> chatList = supportChatRepo.findAllByCustomer(user);
			List<ExpiryProductDto> list = new ArrayList<>();
			Set<String> ItemIds = new HashSet<>();

			for (SupportChat chat : chatList) {

				String itemId = chat.getOrderItemId().getId();
				if (ItemIds.contains(itemId)) {
					continue;
				}

				ItemIds.add(itemId);

				ExpiryProductDto dto = new ExpiryProductDto();
				dto.setId(itemId);
				dto.setPruchaseDate(chat.getOrderItemId().getOrder().getDate());
				dto.setOrderItemName(chat.getOrderItemId().getProductName());
				dto.setProductImage(chat.getOrderItemId().getProductImageUrl());
				dto.setSource(chat.getOrderItemId().getOrder().getCustomerSourceMap().getSource().getName());
				list.add(dto);
			}

			return new DefaultListResponse(true, "All Chat support History for Customer", list);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while support chat products", null);
		}
	}

//	public ApiResponse uploadReceipt(MultipartFile file, String customerId) {
//	    try {
//
//	        Path uploadPath = Paths.get(uploadDir);
//	        Files.createDirectories(uploadPath);
//
//
//	        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//	        Path filePath = uploadPath.resolve(fileName);
//
//
//	        try (InputStream inputStream = file.getInputStream()) {
//	            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
//	        }
//
//	        FileSystemResource fileResource = new FileSystemResource(filePath.toFile());
//	        long fileSize = fileResource.contentLength();
//
//	        log.info("File saved: {} ({} bytes)", filePath.toAbsolutePath(), fileSize);
//
//
//	        String pythonApiUrl = "https://ocr.entitleguard.com/purchases";
//	        log.info("Calling OCR API at: {}", pythonApiUrl);
//
//	        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//	        body.add("file", fileResource);
//
//	        HttpHeaders headers = new HttpHeaders();
//	        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//	        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//	        RestTemplate restTemplate = new RestTemplate();
//
//	        ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
//	        log.info("OCR API Response: {}", response);
//
//	        Map<String, Object> data = response.getBody();
//
//	        if (data == null || data.isEmpty()) {
//	            return new ApiResponse(false, "No response from OCR service");
//	        }
//
//	        if (!customerRepo.existsById(customerId)) {
//	            return new ApiResponse(false, "Invalid customer ID: " + customerId);
//	        }
//
//	        saveOrderFromPythonResponse(data, customerId, fileName);
//
//	        return new ApiResponse(true, "Order created successfully");
//
//	    } catch (IOException ioEx) {
//	        log.error("File upload error: {}", ioEx.getMessage(), ioEx);
//	        return new ApiResponse(false, "File upload failed: " + ioEx.getMessage());
//	    } catch (Exception ex) {
//	        log.error("Error processing receipt upload: {}", ex.getMessage(), ex);
//	        return new ApiResponse(false, "Error: " + ex.getMessage());
//	    }
//	}

	public DefaultListResponse uploadReceipt(MultipartFile file, String customerId) {
		try {
			if (!customerRepo.existsById(customerId)) {
				return new DefaultListResponse(false, "Invalid customer ID: " + customerId,null);
			}

			Resource resource = new ByteArrayResource(file.getBytes()) {
				@Override
				public String getFilename() {
					return file.getOriginalFilename();
				}
			};

			String fileName = (file != null && file.getOriginalFilename() != null) ? file.getOriginalFilename()
					: "default.jpg";

			String pythonApiUrl = "https://ocr.entitleguard.com/bytes";
			log.info("pythonApiUrl {}", pythonApiUrl);

			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
			body.add("file", resource);

			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);

			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
			RestTemplate restTemplate = new RestTemplate();

			ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
			log.info("response {}", response);

			Map<String, Object> data = response.getBody();
			System.out.println("Python API Response: " + data);

			if (data == null || data.isEmpty()) {
				return new DefaultListResponse(false, "No response from Python API",null);
			}

		 	Orders order=  saveOrderFromPythonResponse(data, customerId, fileName);
			return new DefaultListResponse(true, "Order created successfully",order);

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error: " + e.getMessage(),null);
		}
	}

	private Orders saveOrderFromPythonResponse(Map<String, Object> response, String customerId, String uploadedFileName) {
		try {

			String merchantName = (String) response.get("merchant");
			if (merchantName == null || merchantName.isEmpty()) {
				merchantName = (String) response.get("source_name"); // fallback
			}
			if (merchantName == null || merchantName.isEmpty()) {
				merchantName = "Unknown Store";
			}

			Object transactionObj = response.get("transaction");
			Double amountTotal = 0.0;
			if (transactionObj instanceof Map<?, ?> transactionMap) {
				Object totalObj = transactionMap.get("total");
				if (totalObj != null && !totalObj.toString().isBlank()) {
					try {
						amountTotal = Double.valueOf(totalObj.toString());
					} catch (NumberFormatException e) {
						log.warn("Invalid total value in response: {}", totalObj);
					}
				}
			}

			if (amountTotal == 0.0) {
				amountTotal = 0.0; // default or extract subtotal
			}

			Customer customer = customerRepo.findById(customerId)
					.orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));

			Source foundSource = sourceRepo.findByCode(merchantName);
			if (foundSource == null) {
				foundSource = new Source();
				foundSource.setName(merchantName);
				foundSource.setCode(merchantName);
				foundSource = sourceRepo.save(foundSource);
			}

			final Source source = foundSource;
			String referenceId = UUID.randomUUID().toString();

			CustomerSourceMap map = customerSourceMapRepo
					.findByCustomerIdAndSourceIdAndReferenceId(customer.getId(), source.getId(), referenceId)
					.orElseGet(() -> {
						CustomerSourceMap newMap = new CustomerSourceMap();
						newMap.setCustomer(customer);
						newMap.setSource(source);
						newMap.setReferenceId(referenceId);
						return customerSourceMapRepo.save(newMap);
					});

			Orders order = new Orders();
			order.setCustomerSourceMap(map);
			order.setDate(LocalDate.now());
			order.setOrderId(UUID.randomUUID().toString());
			order.setType("PRODUCT");
			order.setStatus("paid");
			order.setFulfilmentStatus("fulfilled");
			order.setTotalPrice(amountTotal);
			order.setActualPrice(amountTotal);

			com.furtim.entitleguard.entity.Files receiptFile = new com.furtim.entitleguard.entity.Files();
			receiptFile.setName(uploadedFileName);
			receiptFile.setFileType("receipt");
			receiptFile.setType("image");
			receiptFile.setFilePath("uploads/" + uploadedFileName);
			receiptFile.setReferenceId(order.getOrderId());
			receiptFile.setIsDeleted(false);
			filesRepo.save(receiptFile);

			order = orderRepo.save(order);

			// ✅ Handle items safely
			List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
			double total = 0.0;
			if (items != null && !items.isEmpty()) {
				for (Map<String, Object> item : items) {
					String productName = (String) item.getOrDefault("name", "Unknown Item");
					Double price = 0.0;
					try {
						price = Double.valueOf(item.getOrDefault("unit_price", "0").toString());
					} catch (NumberFormatException ignored) {
					}

					OrderItem orderItem = new OrderItem();
					orderItem.setOrder(order);
					orderItem.setProductName(productName);
					orderItem.setBrand("Unknown");
					orderItem.setActualPrice(price);
					orderItem.setPrice(price);

					orderItemRepo.save(orderItem);
					total += price;
				}
			}

			order.setTotalPrice(total > 0 ? total : amountTotal);
			order.setActualPrice(total > 0 ? total : amountTotal);
			Orders savedOrder =  orderRepo.save(order);
			return savedOrder;

		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error saving order from Python response: " + e.getMessage());
		}
	}

}
