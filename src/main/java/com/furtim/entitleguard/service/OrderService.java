package com.furtim.entitleguard.service;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furtim.entitleguard.controller.MailController;
import com.furtim.entitleguard.controller.SmsController;
import com.furtim.entitleguard.dto.AddressDto;
import com.furtim.entitleguard.dto.CustomerDto;
import com.furtim.entitleguard.dto.ExpiryProductDto;
import com.furtim.entitleguard.dto.ItemDto;
import com.furtim.entitleguard.dto.OrderDetailDto;
import com.furtim.entitleguard.dto.OrderDto;
import com.furtim.entitleguard.dto.ProductDetailsDto;
import com.furtim.entitleguard.dto.PropertyInformationDto;
import com.furtim.entitleguard.entity.Address;
import com.furtim.entitleguard.entity.BuilderCustomer;
import com.furtim.entitleguard.entity.BuilderCustomerItemMap;
import com.furtim.entitleguard.entity.BuilderItem;
import com.furtim.entitleguard.entity.BuilderOrganization;
import com.furtim.entitleguard.entity.Catlog;
import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.Documents;
import com.furtim.entitleguard.entity.Entitlement;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Orders;
import com.furtim.entitleguard.entity.ProductEntitlement;
import com.furtim.entitleguard.entity.ShopToken;
import com.furtim.entitleguard.entity.Source;
import com.furtim.entitleguard.entity.Status;
import com.furtim.entitleguard.entity.SupportChat;
import com.furtim.entitleguard.repository.AddressRepository;
import com.furtim.entitleguard.repository.BuilderCustomerItemMapRepository;
import com.furtim.entitleguard.repository.BuilderCustomerRepository;
import com.furtim.entitleguard.repository.CatlogRepository;
import com.furtim.entitleguard.repository.CustomerRepository;
import com.furtim.entitleguard.repository.CustomerSourceMapRepository;
import com.furtim.entitleguard.repository.DocumentsRepository;
import com.furtim.entitleguard.repository.EntitlementRepository;
import com.furtim.entitleguard.repository.FilesRepository;
import com.furtim.entitleguard.repository.OrderItemRepository;
import com.furtim.entitleguard.repository.OrderRepository;
import com.furtim.entitleguard.repository.ProductEntitlementRepository;
import com.furtim.entitleguard.repository.SourceRepository;
import com.furtim.entitleguard.repository.StatusRepository;
import com.furtim.entitleguard.repository.SupportChatRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.utils.StatusConst;
import com.furtim.entitleguard.utils.StatusModule;
import com.furtim.entitleguard.utils.UserSessionUtil;

import io.jsonwebtoken.io.IOException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class OrderService {

	private final OrderRepository orderRepo;

	private final OrderItemRepository orderItemRepo;

	private final AddressRepository addressRepo;

	private final SourceRepository sourceRepo;

	private final CustomerSourceMapRepository customerSourceMapRepo;

	private final CustomerRepository customerRepo;

	private final ProductService productService;

	private final EntitlementRepository entitlementRepo;

	private final ProductEntitlementRepository productEntitlementRepo;

	private final SupportChatRepository supportChatRepo;

	private final DocumentsRepository documentsRepo;

	private final MailController mailController;

	private final SmsController smsController;

	private final BuilderCustomerRepository builderCustomerRepo;

	private final BuilderCustomerItemMapRepository builderCustomerItemMapRepo;

	private final CatlogRepository catlogRepo;

	private final StatusRepository statusRepo;

	private final FilesRepository filesRepo;

	private final RestTemplate restTemplate = new RestTemplate();

	public void saveShopifyOrder(JsonNode orderJson, String shop, String accessToken, ShopToken shopToken) {

		JsonNode addressJson = orderJson.get("shipping_address");
		Address shippingAddress = null;
		if (addressJson != null) {
			shippingAddress = new Address();
			shippingAddress.setCity(addressJson.get("city").asText());
			shippingAddress.setApt(addressJson.get("address1").asText());
			shippingAddress.setStreet(addressJson.get("address2").asText());
			shippingAddress.setState(addressJson.get("province").asText());
			shippingAddress.setCountry(addressJson.get("country").asText());
			shippingAddress.setZipCode(addressJson.get("zip").asText());
			shippingAddress = addressRepo.save(shippingAddress);
		}

		String orderId = orderJson.get("id").asText();
		Optional<Orders> orders = orderRepo.findOneByOrderId(orderId);
		Orders order;
		if (orders.isPresent()) {
			order = orders.get();
			order.setShipToAddress(shippingAddress);
			order.setOrderId(orderId);
			order.setShopToken(shopToken);
			order.setDate(LocalDate.now());
			order.setStatus(orderJson.get("financial_status").asText());
			order.setFulfilmentStatus(
					orderJson.get("fulfillment_status").isNull() ? null : orderJson.get("fulfillment_status").asText());
			order.setActualPrice(orderJson.get("subtotal_price").asDouble());
			order.setTotalPrice(orderJson.get("total_price").asDouble());

			JsonNode taxNode = orderJson.get("total_tax");
			order.setTax(taxNode != null ? taxNode.asDouble() : 0.0);

			JsonNode lineItemsNode = orderJson.get("line_items");
			order.setQuantity((double) lineItemsNode.size());

			orderRepo.save(order);
			log.info("Updated existing order: {}", orderId);
			return;
		}
		order = new Orders();
		order.setShipToAddress(shippingAddress);
		order.setOrderId(orderId);
		order.setShopToken(shopToken);
		order.setDate(LocalDate.now());
		order.setStatus(orderJson.get("financial_status").asText());
		order.setFulfilmentStatus(
				orderJson.get("fulfillment_status").isNull() ? null : orderJson.get("fulfillment_status").asText());
		order.setActualPrice(orderJson.get("subtotal_price").asDouble());
		order.setTotalPrice(orderJson.get("total_price").asDouble());

		JsonNode taxNode = orderJson.get("total_tax");
		order.setTax(taxNode != null ? taxNode.asDouble() : 0.0);

		JsonNode lineItemsNode = orderJson.get("line_items");
		order.setQuantity((double) lineItemsNode.size());

		JsonNode customerJson = orderJson.get("customer");
		if (customerJson != null) {
			String email = customerJson.has("email") && !customerJson.get("email").isNull()
					? customerJson.get("email").asText()
					: null;
			String phone = customerJson.has("phone") && !customerJson.get("phone").isNull()
					? customerJson.get("phone").asText()
					: null;

			Optional<Customer> optionalCustomer = Optional.empty();

			if (email != null) {
				optionalCustomer = Optional.ofNullable(customerRepo.findByEmail(email));
			}
			if (optionalCustomer.isEmpty() && phone != null) {
				optionalCustomer = Optional.ofNullable(customerRepo.findByContact(phone));
			}

			final boolean isNewCustomer = optionalCustomer.isEmpty();

			Customer customer = optionalCustomer.orElseGet(() -> {
				Customer newCustomer = new Customer();
				newCustomer.setName(
						customerJson.get("first_name").asText() + " " + customerJson.get("last_name").asText());
				newCustomer.setEmail(email);
				newCustomer.setContact(phone);
				newCustomer.setIsRegistered(!"disabled".equalsIgnoreCase(customerJson.get("state").asText()));

				JsonNode defaultAddressJson = customerJson.get("default_address");
				if (defaultAddressJson != null) {
					Address customerAddress = new Address();
					customerAddress.setCity(defaultAddressJson.get("city").asText());
					customerAddress.setCountry(defaultAddressJson.get("country").asText());
					customerAddress.setZipCode(defaultAddressJson.get("zip").asText());
					customerAddress = addressRepo.save(customerAddress);
					newCustomer.setAddress(customerAddress);
				}

				return customerRepo.save(newCustomer);
			});

			if (isNewCustomer) {
				if (email != null) {
					mailController.sendWelcomeEmail(customer);
				}
				if (phone != null) {
					smsController.sendWelcomeMessage(phone, customer.getName());
				}
			}

			Optional<Source> optionalSource = Optional.ofNullable(sourceRepo.findByCode("shopify"));
			Source source = optionalSource.orElseGet(() -> {
				Source newSource = new Source();
				newSource.setCode("shopify");
				return sourceRepo.save(newSource);
			});

			final String referenceId = customerJson.get("id").asText();
			Optional<CustomerSourceMap> optionalMap = customerSourceMapRepo
					.findByCustomerIdAndSourceIdAndReferenceId(customer.getId(), source.getId(), referenceId);

			final Customer finalCustomer = customer;
			final Source finalSource = source;

			CustomerSourceMap customerSourceMap = optionalMap.orElseGet(() -> {
				CustomerSourceMap newMap = new CustomerSourceMap();
				newMap.setCustomer(finalCustomer);
				newMap.setSource(finalSource);
				newMap.setReferenceId(referenceId);
				return customerSourceMapRepo.save(newMap);
			});

			order.setCustomerSourceMap(customerSourceMap);
		}

		order = orderRepo.save(order);

		for (JsonNode itemJson : lineItemsNode) {
			OrderItem item = new OrderItem();
			item.setOrder(order);
			item.setProductId(itemJson.get("product_id").asText());
			item.setProductName(itemJson.get("name").asText());
			item.setSku(itemJson.has("sku") && !itemJson.get("sku").isNull() ? itemJson.get("sku").asText() : null);
			item.setBrand(itemJson.get("vendor").asText());
			item.setPrice(itemJson.get("price").asDouble());
			item.setActualPrice(itemJson.get("price").asDouble());
			item.setQuantity(itemJson.get("quantity").asDouble());
			item.setCurrency("INR");
			item.setUnit("unit");

			JsonNode taxLines = itemJson.get("tax_lines");
			if (taxLines.isArray() && taxLines.size() > 0) {
				item.setTax(taxLines.get(0).get("price").asDouble());
			}

			JsonNode discountNode = itemJson.get("total_discount");
			item.setDiscount(discountNode != null ? discountNode.asDouble() : 0.0);

			orderItemRepo.save(item);

			List<ProductEntitlement> entitlement = productService.getProductWithMetafields(shop, "2025-04", accessToken,
					itemJson.get("product_id").asText(), item);

			log.info("product{}", entitlement);

		}
	}

	public DefaultListResponse getProductByCustomer() {
		try {
			String customer = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customer);

			LocalDate today = LocalDate.now();
			List<ExpiryProductDto> list = new ArrayList<>();

			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());
				List<Entitlement> entitlements = entitlementRepo.findAll();
				for (CustomerSourceMap csm : customerSourceMaps) {
					log.info("csm id {}", csm.getId());

					List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerSourceMap(true, csm);

					for (Orders order : orders) {
						log.info("order id {}", order.getId());

						List<OrderItem> orderItems = orderItemRepo.findAllByOrder(order);

						for (OrderItem ot : orderItems) {
							log.info("orderItem id {}", ot.getId());

							for (Entitlement entitlement : entitlements) {
								Optional<ProductEntitlement> productEntitlementOpt = productEntitlementRepo
										.findOneByOrderItemAndEntitlement(ot, entitlement);

								if (productEntitlementOpt.isPresent()) {
									ProductEntitlement product = productEntitlementOpt.get();
									LocalDate expiryDate = product.getEntitlementExpiryDate();
									LocalDate oneMonthBeforeExpiry = expiryDate.minusMonths(1);

									if ((today.isEqual(oneMonthBeforeExpiry) || today.isAfter(oneMonthBeforeExpiry))
											&& (today.isBefore(expiryDate) || today.isEqual(expiryDate))) {

										log.info("expiring {} for product id {}", entitlement.getName(),
												product.getId());

										ExpiryProductDto dto = new ExpiryProductDto();
										dto.setId(ot.getId());
										dto.setSource(csm.getSource().getName());
										dto.setOrderItemName(ot.getProductName());
										dto.setProductImage(ot.getProductImageUrl());
										dto.setEntitlementType(entitlement.getName());
										Integer daysToExpiry = (int) ChronoUnit.DAYS.between(today, expiryDate);
										dto.setExpiryDays(daysToExpiry);
										list.add(dto);
									}
								}
							}
						}
					}
				}

				return new DefaultListResponse(true, "Product List", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product details", null);
		}
	}

	public DefaultListResponse getAllProductByCustomer() {
		try {

			String customer = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customer);
			List<ExpiryProductDto> list = new ArrayList<>();

			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());

				for (CustomerSourceMap csm : customerSourceMaps) {
					log.info("CustomerSourceMap ID: {}", csm.getId());

					List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerSourceMap(true, csm);

					for (Orders order : orders) {
						log.info("Order ID: {}", order.getId());

						List<OrderItem> orderItems = orderItemRepo.findAllByOrder(order);

						for (OrderItem ot : orderItems) {
							log.info("OrderItem ID: {}", ot.getId());

							ExpiryProductDto dto = new ExpiryProductDto();
							dto.setId(ot.getId());
							if (order.getOrderId() != null) {
								dto.setOrderId(order.getOrderId());
								dto.setBuilderOrderId(order.getId());
							}
							dto.setPruchaseDate(ot.getOrder().getDate());
							dto.setOrderItemName(ot.getProductName());
							dto.setProductImage(ot.getProductImageUrl());
							dto.setSource(csm.getSource().getName());

							list.add(dto);
						}
					}
				}
				list.sort((a, b) -> b.getPruchaseDate().compareTo(a.getPruchaseDate()));

				return new DefaultListResponse(true, "All Products for Customer", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving products", null);
		}
	}

	public DefaultListResponse getProductDetails(String orderItemId) {
		try {
			Optional<OrderItem> optionalItem = orderItemRepo.findById(orderItemId);
			if (optionalItem.isPresent()) {
				OrderItem item = optionalItem.get();

				ProductDetailsDto dto = new ProductDetailsDto();
				dto.setId(item.getId());
				dto.setOrderId(item.getOrder().getOrderId());
				dto.setSku(item.getSku());
				if (item.getOrder().getShopToken() != null) {
					dto.setVendor(item.getOrder().getShopToken().getShop());
				}

				dto.setOrderDate(item.getOrder().getDate());
				dto.setProductImage(item.getProductImageUrl());
				dto.setProductName(item.getProductName());
				dto.setProductDesc(item.getProductDesc());
				dto.setProductType(item.getProductType());
				dto.setSource(item.getOrder().getCustomerSourceMap().getSource().getName());
				LocalDate today = LocalDate.now();

				List<ProductEntitlement> entitlements = productEntitlementRepo.findAllByOrderItemId(item.getId());
				for (ProductEntitlement ent : entitlements) {
					Entitlement e = ent.getEntitlement();
					if (e.getName().equalsIgnoreCase("warranty")) {
						if (ent.getEntitlementExpiryDate() != null && !ent.getEntitlementExpiryDate().isBefore(today)) {
							Integer daysToExpiry = (int) ChronoUnit.DAYS.between(today, ent.getEntitlementExpiryDate());
							dto.setWarrantyExpiryDays(daysToExpiry);
							dto.setWarrentyStatus("Eligible");
						} else {
							dto.setWarrentyStatus("Expired");
						}
						dto.setWarrantyDays(ent.getEntitlementPeriodValue() + " " + ent.getEntitlementPeriodType());
						dto.setWarrantyExpiryDate(ent.getEntitlementExpiryDate());

						if (ent.getEntitlementDocuments() != null) {
							dto.setWarrantyFile(ent.getEntitlementDocuments().getLocation() != null
									? ent.getEntitlementDocuments().getLocation()
									: null);
						}

					} else if (e.getName().equalsIgnoreCase("return_policy")) {

						if (ent.getEntitlementExpiryDate() != null && !ent.getEntitlementExpiryDate().isBefore(today)) {
							Integer daysToExpiry = (int) ChronoUnit.DAYS.between(today, ent.getEntitlementExpiryDate());
							dto.setReturnStatus("Eligible");
							dto.setReturnExpiryDays(daysToExpiry);
						} else {
							dto.setReturnStatus("Expired");
						}
						dto.setReturnDay(ent.getEntitlementPeriodValue() + " " + ent.getEntitlementPeriodType());
						dto.setReturnExpiryDate(ent.getEntitlementExpiryDate());
						if (ent.getEntitlementDocuments() != null) {
							dto.setReturnPolicyFile(ent.getEntitlementDocuments().getLocation() != null
									? ent.getEntitlementDocuments().getLocation()
									: null);
						}

					}
				}

				Customer customer = item.getOrder().getCustomerSourceMap().getCustomer();
				CustomerDto customerDto = new CustomerDto();
				customerDto.setId(customer.getId());
				customerDto.setName(customer.getName());
				customerDto.setEmail(customer.getEmail());
				customerDto.setDob(customer.getDob());
				customerDto.setContact(customer.getContact());

				Address address = item.getOrder().getShipToAddress();
				if (address == null) {
					address = customer.getAddress();
				}

				if (address != null) {
					AddressDto addressDto = new AddressDto();
					addressDto.setApt(address.getApt());
					addressDto.setStreet(address.getStreet());
					addressDto.setCity(address.getCity());
					addressDto.setState(address.getState());
					addressDto.setCountry(address.getCountry());
					addressDto.setZipCode(address.getZipCode());
					customerDto.setAddressDto(addressDto);
				}

				dto.setCustomerDto(customerDto);

				return new DefaultListResponse(true, "Product details fetched", dto);
			} else {
				return new DefaultListResponse(false, "Invalid Order Item ID", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving product details", null);
		}
	}

	public DefaultListResponse getOrderDetails(String orderId) {
		try {
			Optional<Orders> order = orderRepo.findOneByOrderId(orderId);
			if (order.isPresent()) {
				Orders orderOptional = order.get();
//				Orders orderOptional = updateOrderFromShopify(orderId);
				OrderDetailDto dto = new OrderDetailDto();
				dto.setId(orderOptional.getId());
				dto.setOrderId(orderOptional.getOrderId());
				dto.setOrderStatus(orderOptional.getStatus());
				dto.setFulfilmentStatus(orderOptional.getFulfilmentStatus());
				dto.setOrderDate(orderOptional.getDate());
				log.info("order update {}", orderOptional.getId());

				List<ItemDto> list = new ArrayList<>();
				List<OrderItem> items = orderItemRepo.findAllByOrder(orderOptional);
				for (OrderItem item : items) {
					log.info("items {}", item.getId());
					ItemDto itemDto = new ItemDto();
					itemDto.setOrderItemId(item.getId());
					itemDto.setBrand(item.getBrand());
					itemDto.setItemName(item.getProductName());
					itemDto.setItemImage(item.getProductImageUrl());
					list.add(itemDto);

				}
				dto.setItems(list);

				Customer customer = orderOptional.getCustomerSourceMap().getCustomer();
				CustomerDto customerDto = new CustomerDto();
				customerDto.setId(customer.getId());
				customerDto.setName(customer.getName());
				customerDto.setEmail(customer.getEmail());
				customerDto.setDob(customer.getDob());
				customerDto.setContact(customer.getContact());

				Address address = orderOptional.getShipToAddress();
				if (address == null) {
					address = customer.getAddress();
				}

				if (address != null) {
					AddressDto addressDto = new AddressDto();
					addressDto.setApt(address.getApt());
					addressDto.setStreet(address.getStreet());
					addressDto.setCity(address.getCity());
					addressDto.setState(address.getState());
					addressDto.setCountry(address.getCountry());
					addressDto.setZipCode(address.getZipCode());
					customerDto.setAddressDto(addressDto);
				}

				dto.setStoreName(orderOptional.getShopToken().getShop());
				dto.setCustomerDto(customerDto);

				return new DefaultListResponse(true, "order details fetched", dto);
			} else {
				return new DefaultListResponse(true, "Invalid order id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving order details", null);
		}
	}

	private Orders updateOrderFromShopify(String orderId) {
		try {
			Optional<Orders> optionalOrder = orderRepo.findOneByOrderId(orderId);
			if (!optionalOrder.isPresent()) {
				return null;
			}

			Orders existingOrder = optionalOrder.get();
			String shop = existingOrder.getShopToken().getShop();
			String accessToken = existingOrder.getShopToken().getAccessToken();
			String orderUrl = String.format("https://%s/admin/api/2025-04/orders.json?id=%s", shop, orderId);

			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Shopify-Access-Token", accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(orderUrl, HttpMethod.GET, request, String.class);

			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());
			JsonNode orders = root.get("orders");

			if (orders != null && orders.isArray()) {
				for (JsonNode orderJson : orders) {
					saveShopifyOrder(orderJson, shop, accessToken, existingOrder.getShopToken());
					String savedOrderId = orderJson.get("id").asText();
					Optional<Orders> updatedOrder = orderRepo.findOneByOrderId(savedOrderId);
					return updatedOrder.get();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public DefaultListResponse getproductByProductCategoryAndCustomer(String productCategory) {
		try {

			String customer = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customer);
			List<ExpiryProductDto> list = new ArrayList<>();

			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());

				for (CustomerSourceMap csm : customerSourceMaps) {
					log.info("CustomerSourceMap ID: {}", csm.getId());

					List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerSourceMap(true, csm);

					for (Orders order : orders) {
						log.info("Order ID: {}", order.getId());

						List<OrderItem> orderItems = orderItemRepo.findAllByOrderAndProductType(order, productCategory);

						for (OrderItem ot : orderItems) {
							log.info("OrderItem ID: {}", ot.getId());

							ExpiryProductDto dto = new ExpiryProductDto();
							dto.setId(ot.getId());
							dto.setPruchaseDate(ot.getOrder().getDate());
							dto.setOrderItemName(ot.getProductName());
							dto.setProductImage(ot.getProductImageUrl());
							dto.setSource(csm.getSource().getName());

							list.add(dto);
						}
					}
				}
				list.sort((a, b) -> b.getPruchaseDate().compareTo(a.getPruchaseDate()));

				return new DefaultListResponse(true, "All Products for Customer", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving products", null);
		}
	}

	public DefaultListResponse getProductBySearch(String search, String productType) {
		try {
			String customerId = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customerId);
			List<ExpiryProductDto> list = new ArrayList<>();
			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());
				List<OrderItem> orderItems = orderItemRepo.findAllBySearch(search, customerSourceMaps, productType);
				for (OrderItem ot : orderItems) {
					ExpiryProductDto dto = new ExpiryProductDto();
					dto.setId(ot.getId());
					dto.setOrderItemName(ot.getProductName());
					dto.setPruchaseDate(ot.getOrder().getDate());
					dto.setProductImage(ot.getProductImageUrl());
					dto.setSource(ot.getOrder().getCustomerSourceMap().getSource().getName());
					list.add(dto);

				}

				list.sort((a, b) -> b.getPruchaseDate().compareTo(a.getPruchaseDate()));
				return new DefaultListResponse(true, "Product fertch successfully", list);

			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product ", null);
		}
	}

	public DefaultListResponse getProductCategoriesBySearch(String search) {
		try {
			String customerId = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customerId);
			Set<String> productCategoryName = new HashSet<>();
			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());
				List<OrderItem> orderItems = orderItemRepo.findAllProductTypeBySearch(search, customerSourceMaps);
				for (OrderItem ot : orderItems) {
					productCategoryName.add(ot.getProductType());

				}
				List<String> productCategories = new ArrayList<>(productCategoryName);
				return new DefaultListResponse(true, "Product categories fertch successfully", productCategories);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product categories ", null);
		}
	}

	public DefaultListResponse getAllOrdersByCustomer() {
		try {
			String customerId = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customerId);
			if (cus.isPresent()) {
				List<OrderDto> list = new ArrayList<>();
				List<Orders> orders = orderRepo.findAllByIsActiveAndCustomer(true, cus.get());
				for (Orders order : orders) {
					OrderDto dto = new OrderDto();
					dto.setId(order.getId());
					dto.setOrderId(order.getOrderId());
					dto.setOrderDate(order.getDate());
					dto.setSource(order.getCustomerSourceMap().getSource().getName());
					Integer count = orderItemRepo.countByOrder(order);
					dto.setNoOfItem(count);
					list.add(dto);

				}
				list.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));
				return new DefaultListResponse(true, "Orders fertch successfully", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting orders ", null);
		}
	}

	public ApiResponse deleteOrderById(String orderid) {
		try {
			Optional<Orders> order = orderRepo.findOneByOrderId(orderid);
			if (order.isPresent()) {
				List<OrderItem> items = orderItemRepo.findAllByOrder(order.get());
				for (OrderItem ot : items) {
					ot.setProductManual(null);
					ot.setExtraFiles(null);
					orderItemRepo.save(ot);

					List<ProductEntitlement> productEntitle = productEntitlementRepo.findAllByOrderItemId(ot.getId());
					List<SupportChat> sChats = supportChatRepo.findAllByOrderItemId(ot.getId());
					List<Documents> documents = documentsRepo.findAllByOrderItemId(ot.getId());

					documentsRepo.deleteAll(documents);
					supportChatRepo.deleteAll(sChats);
					productEntitlementRepo.deleteAll(productEntitle);
					orderItemRepo.delete(ot);
				}

				orderRepo.delete(order.get());
				return new ApiResponse(true, "Order deleted successfully");
			} else {
				return new ApiResponse(false, "Invalid Order Id");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong while deleting the order");

		}
	}

	public DefaultListResponse getWarrentyProduct() {
		try {
			String customer = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customer);

			LocalDate today = LocalDate.now();
			List<ExpiryProductDto> list = new ArrayList<>();

			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());
				Optional<Entitlement> entitlementOpt = entitlementRepo.findOneByName("warranty");

				if (entitlementOpt.isEmpty()) {
					return new DefaultListResponse(false, "Warranty entitlement not found", null);
				}

				Entitlement entitlement = entitlementOpt.get();

				for (CustomerSourceMap csm : customerSourceMaps) {
					log.info("csm id {}", csm.getId());

					List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerSourceMap(true, csm);

					for (Orders order : orders) {
						log.info("order id {}", order.getId());

						List<OrderItem> orderItems = orderItemRepo.findAllByOrder(order);

						for (OrderItem ot : orderItems) {
							log.info("orderItem id {}", ot.getId());

							Optional<ProductEntitlement> productEntitlementOpt = productEntitlementRepo
									.findOneByOrderItemAndEntitlement(ot, entitlement);

							if (productEntitlementOpt.isPresent()) {
								ProductEntitlement product = productEntitlementOpt.get();
								LocalDate expiryDate = product.getEntitlementExpiryDate();

								if (today.isAfter(expiryDate)) {
									continue;
								}

								log.info("active warranty for product id {}", product.getId());

								ExpiryProductDto dto = new ExpiryProductDto();
								dto.setId(ot.getId());
								dto.setSource(csm.getSource().getName());
								dto.setOrderItemName(ot.getProductName());
								dto.setProductImage(ot.getProductImageUrl());
								dto.setEntitlementType(entitlement.getName());
								dto.setExpiryDays((int) ChronoUnit.DAYS.between(today, expiryDate));
								list.add(dto);
							}
						}
					}
				}

				return new DefaultListResponse(true, "Product List", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product details", null);
		}
	}

	public ApiResponse createOrderFromBuilderCustomer(String builderCustomerId) {
		try {

			BuilderCustomer builderCustomer = builderCustomerRepo.findById(builderCustomerId)
					.orElseThrow(() -> new RuntimeException("Builder Customer not found"));

			List<BuilderCustomerItemMap> itemMappings = builderCustomerItemMapRepo
					.findByBuilderCustomerId(builderCustomerId);
			if (itemMappings.isEmpty()) {
				throw new RuntimeException("No items mapped for this builder customer");
			}

			Source source = null;
			if (builderCustomer.getBuilderOrganization() != null) {
				BuilderOrganization builder = builderCustomer.getBuilderOrganization();
				source = sourceRepo.findByCode(builder.getName());

				if (source == null) {
					source = new Source();
					source.setName(builder.getName());
					source.setEmail(builder.getEmail());
					source.setCode(builder.getName());
					source.setContact(builder.getContact());

					Address address = new Address();
					address.setStreet(builder.getAddress());
					address = addressRepo.save(address);

					source.setAddress(address);
					source = sourceRepo.save(source);
				}
			}

			Customer customer = customerRepo.findByEmail(builderCustomer.getEmail());
			if (customer == null) {
				customer = new Customer();
				customer.setName(builderCustomer.getFirstName() + " " + builderCustomer.getLastName());
				customer.setEmail(builderCustomer.getEmail());
				customer.setContact(builderCustomer.getContact());
				customer.setIsRegistered(true);

				Address address = new Address();
				address.setCity(builderCustomer.getCity());
				address.setState(builderCustomer.getState());
				address.setCountry(builderCustomer.getCountry());
				address.setZipCode(builderCustomer.getZip());
				address.setStreet(builderCustomer.getAddress());
				address = addressRepo.save(address);

				customer.setAddress(address);
				customer = customerRepo.save(customer);
			}

			final Customer finalCustomer = customer;
			final Source finalSource = source;

			String referenceId = builderCustomer.getId();
			CustomerSourceMap customerSourceMap = customerSourceMapRepo
					.findByCustomerIdAndSourceIdAndReferenceId(finalCustomer.getId(), finalSource.getId(), referenceId)
					.orElseGet(() -> {
						CustomerSourceMap map = new CustomerSourceMap();
						map.setCustomer(finalCustomer);
						map.setSource(finalSource);
						map.setReferenceId(referenceId);
						return customerSourceMapRepo.save(map);
					});

			Orders order = new Orders();
			order.setCustomerSourceMap(customerSourceMap);
			order.setDate(LocalDate.now());
			order.setProperty(builderCustomer.getProjectName());
			order.setStatus("paid");
			order.setFulfilmentStatus("fulfilled");
			order.setType("PROPERTY");
			order.setActualPrice(0.0);
			order.setTotalPrice(0.0);
			order.setTax(0.0);
			order.setQuantity((double) itemMappings.size());

			Address shipToAddress = new Address();
			shipToAddress.setCity(builderCustomer.getCity());
			shipToAddress.setState(builderCustomer.getState());
			shipToAddress.setStreet(builderCustomer.getAddress());
			shipToAddress.setZipCode(builderCustomer.getZip());
			shipToAddress = addressRepo.save(shipToAddress);

			order.setShipToAddress(shipToAddress);
			order.setOrderId(UUID.randomUUID().toString());
			order = orderRepo.save(order);

			double total = 0.0;
			for (BuilderCustomerItemMap map : itemMappings) {
				BuilderItem item = map.getBuilderItem();

				OrderItem orderItem = new OrderItem();
				orderItem.setOrder(order);
				orderItem.setProductId(item.getId());
				orderItem.setProductName(item.getName());
				orderItem.setBrand(item.getBrand());
				orderItem.setSku(item.getModel());
				orderItem.setPrice(item.getPrice() != null ? Double.parseDouble(item.getPrice()) : 0.0);
				orderItem.setActualPrice(orderItem.getPrice());
				orderItem.setQuantity(1.0);
				orderItem.setCurrency("INR");
				orderItem.setUnit("unit");
				orderItem.setDiscount(0.0);
				orderItem.setProductImageUrl(item.getDocumentationUrl());
				orderItemRepo.save(orderItem);

				total += orderItem.getPrice();
			}

			order.setTotalPrice(total);
			order.setActualPrice(total);
			orderRepo.save(order);
			boolean emailSent = mailController.sendWelcomeEmail(customer);
			if (emailSent) {
				log.info("Order confirmation email sent successfully to {}", customer.getEmail());
			} else {
				log.warn("Failed to send order confirmation email to {}", customer.getEmail());
			}

			Status entitlement = statusRepo.findOneByModuleAndName(StatusModule.BUILDER.toString(),
					StatusConst.ENTITLEMENT.toString());
			if (entitlement != null) {
				builderCustomer.setStatus(entitlement);
				builderCustomerRepo.save(builderCustomer);
				log.info("BuilderCustomer status updated to ENTITLEMENT for ID: {}", builderCustomer.getId());
			}
			return new ApiResponse(true, "Order created for builder customer successfully");
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong during getting product details");
		}
	}

	public DefaultListResponse getCatlog() {
		try {
			List<Catlog> catlogs = catlogRepo.findAllByIsActive(true);

			// Create a map to group items by category
			Map<String, List<Catlog>> categoryMap = new HashMap<>();

			for (Catlog item : catlogs) {
				String category = item.getCategory();
				if (!categoryMap.containsKey(category)) {
					categoryMap.put(category, new ArrayList<>());
				}
				categoryMap.get(category).add(item);
			}

			// Convert to list of maps
			List<Map<String, Object>> result = new ArrayList<>();
			for (Map.Entry<String, List<Catlog>> entry : categoryMap.entrySet()) {
				Map<String, Object> group = new HashMap<>();
				group.put("categoryName", entry.getKey());
				group.put("itemCount", entry.getValue().size());
				group.put("items", entry.getValue());
				result.add(group);
			}

			return new DefaultListResponse(true, "Catlog List", result);

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product details", null);
		}
	}

//	public ApiResponse uploadReceipt(MultipartFile file, String customerId) {
//		try {
//			String uploadDir = System.getProperty("user.dir") + "/uploads/";
//			Files.createDirectories(Paths.get(uploadDir));
//			String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//			Path filePath = Paths.get(uploadDir + fileName);
//			Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
//			log.info("file {}", filePath);
//
//			String pythonApiUrl = "https://ocr.entitleguard.com/purchases";
//
//			MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//			body.add("file", new FileSystemResource(filePath.toFile()));
//
//			HttpHeaders headers = new HttpHeaders();
//			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//			HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//			RestTemplate restTemplate = new RestTemplate();
//			log.info("pythonApiUrl {}", pythonApiUrl);
//			ResponseEntity<Map> response = restTemplate.postForEntity(pythonApiUrl, requestEntity, Map.class);
//			log.info("response {}", response);
//			Map<String, Object> data = response.getBody();
//
//			System.out.println("Python API Response: " + data);
//
//			if (data == null || data.isEmpty()) {
//				return new ApiResponse(false, "No response from Python API");
//			}
//
//			if (!customerRepo.existsById(customerId)) {
//				return new ApiResponse(false, "Invalid customer ID: " + customerId);
//			}
//
//			saveOrderFromPythonResponse(data, customerId, fileName);
//			return new ApiResponse(true, "Order created successfully");
//
//		} catch (Exception e) {
//			e.printStackTrace();
//			return new ApiResponse(false, "Error: " + e.getMessage());
//		}
//	}
	
	

	
	public DefaultListResponse uploadManualProperty(PropertyInformationDto propertyDto) {
		try {
			Source source = sourceRepo.findByCode(propertyDto.getBuilderName());
			if (source == null) {
				source = new Source();
				source.setName(propertyDto.getBuilderName());
				source.setCode(propertyDto.getBuilderName().toUpperCase().replaceAll("\\s+", "_"));
				source = sourceRepo.save(source);
			}

			Customer customer = customerRepo.findById(propertyDto.getCustomerId()).orElseThrow(
					() -> new RuntimeException("Customer not found with ID: " + propertyDto.getCustomerId()));

			CustomerSourceMap customerSourceMap = customerSourceMapRepo.findByCustomerAndSource(customer, source)
					.orElse(null);

			if (customerSourceMap == null) {
				customerSourceMap = new CustomerSourceMap();
				customerSourceMap.setCustomer(customer);
				customerSourceMap.setSource(source);
				customerSourceMap.setReferenceId(UUID.randomUUID().toString());
				customerSourceMap = customerSourceMapRepo.save(customerSourceMap);
			}

			Address address = new Address();
			address.setStreet(propertyDto.getPropertyAddress());
			address.setCity(propertyDto.getCity());
			address.setState(propertyDto.getState());
			address.setZipCode(propertyDto.getPostcode());
			address = addressRepo.save(address);

			Orders order = new Orders();
			order.setCustomerSourceMap(customerSourceMap);
			order.setOrderId(propertyDto.getProjectName());
			order.setOrderId(UUID.randomUUID().toString());
			order.setDate(LocalDate.now());
			order.setStatus("PENDING");
			order.setType("PROPERTY");
			order.setFulfilmentStatus("UNFULFILLED");
			order.setQuantity(1.0);
			order.setTax(0.0);
			order.setTotalPrice(0.0);
			order.setActualPrice(0.0);
			order.setShipToAddress(address);

			orderRepo.save(order);

			return new DefaultListResponse(true, "Order created successfully", order);

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error creating order: " + e.getMessage(), null);
		}
	}

	public DefaultListResponse getAllTypeOrders(String type) {
		try {
			String customerId = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customerId);
			if (cus.isPresent()) {
				List<OrderDto> list = new ArrayList<>();
				List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerAndType(true, cus.get(), type);
				for (Orders order : orders) {
					OrderDto dto = new OrderDto();
					dto.setId(order.getId());
					dto.setOrderId(order.getOrderId());
					dto.setAddress(order.getShipToAddress());
					dto.setOrderDate(order.getDate());
					dto.setSource(order.getCustomerSourceMap().getSource().getName());
					Integer count = orderItemRepo.countByOrder(order);
					dto.setNoOfItem(count);
					list.add(dto);

				}
				list.sort((a, b) -> b.getOrderDate().compareTo(a.getOrderDate()));
				return new DefaultListResponse(true, "Orders fertch successfully", list);
			} else {
				return new DefaultListResponse(false, "Invalid Customer Id", null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting orders ", null);
		}
	}

	public ApiResponse addPropertyItems(String orderId, List<String> catalogIds) {
		try {
			Optional<Orders> order = orderRepo.findById(orderId);
			if (order.isPresent()) {
				List<OrderItem> items = orderItemRepo.findAllByOrder(order.get());
				if (!items.isEmpty()) {
					orderItemRepo.deleteAll(items);
				}
				for (String catlogId : catalogIds) {
					Optional<Catlog> catlog = catlogRepo.findOneById(catlogId);
					if (catlog.isPresent()) {
						OrderItem item = new OrderItem();
						item.setOrder(order.get());
						item.setProductId(catlog.get().getId());
						item.setProductName(catlog.get().getName());
						item.setProductType(catlog.get().getCategory());
						item.setProductDesc(catlog.get().getText());
						item.setBrand(catlog.get().getBrand());
						item.setSku(catlog.get().getModel());
						item.setPrice(
								Double.parseDouble(catlog.get().getPrice() != null ? catlog.get().getPrice() : "0.0"));
						item.setDiscount(0.0);
						item.setQuantity(1.0);
						item.setUnit("Unit");
						item.setTax(0.0);
						item.setProductImageUrl(catlog.get().getDocumentationUrl());

						orderItemRepo.save(item);
					}
				}
				return new ApiResponse(true, "Order created successfully");
			} else {
				return new ApiResponse(false, "Invalid Id");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ApiResponse(false, "Something went wrong during getting orders ");
		}
	}

	public DefaultListResponse getOrderDetailsById(String id) {
		try {
			Optional<Orders> order = orderRepo.findOneById(id);
			if (order.isPresent()) {
				Orders orderOptional = order.get();
				OrderDetailDto dto = new OrderDetailDto();
				dto.setId(orderOptional.getId());
				dto.setPropertyName(orderOptional.getProperty());
				dto.setOrderId(orderOptional.getOrderId());
				dto.setOrderStatus(orderOptional.getStatus());
				dto.setFulfilmentStatus(orderOptional.getFulfilmentStatus());
				dto.setOrderDate(orderOptional.getDate());
				log.info("order update {}", orderOptional.getId());

				List<ItemDto> list = new ArrayList<>();
				List<OrderItem> items = orderItemRepo.findAllByOrder(orderOptional);
				for (OrderItem item : items) {
					log.info("items {}", item.getId());
					ItemDto itemDto = new ItemDto();
					itemDto.setOrderItemId(item.getId());
					itemDto.setBrand(item.getBrand());
					itemDto.setItemName(item.getProductName());
					itemDto.setItemImage(item.getProductImageUrl());
					list.add(itemDto);

				}
				dto.setItems(list);

				Customer customer = orderOptional.getCustomerSourceMap().getCustomer();
				CustomerDto customerDto = new CustomerDto();
				customerDto.setId(customer.getId());
				customerDto.setName(customer.getName());
				customerDto.setEmail(customer.getEmail());
				customerDto.setDob(customer.getDob());
				customerDto.setContact(customer.getContact());

				Address address = orderOptional.getShipToAddress();
				if (address == null) {
					address = customer.getAddress();
				}

				if (address != null) {
					AddressDto addressDto = new AddressDto();
					addressDto.setApt(address.getApt());
					addressDto.setStreet(address.getStreet());
					addressDto.setCity(address.getCity());
					addressDto.setState(address.getState());
					addressDto.setCountry(address.getCountry());
					addressDto.setZipCode(address.getZipCode());
					customerDto.setAddressDto(addressDto);
				}

				dto.setCustomerDto(customerDto);

				return new DefaultListResponse(true, "order details fetched", dto);
			} else {
				return new DefaultListResponse(true, "Invalid order id", null);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Error occurred while retrieving order details", null);
		}
	}

}
