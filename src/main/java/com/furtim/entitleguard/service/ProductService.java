package com.furtim.entitleguard.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.Documents;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Orders;
import com.furtim.entitleguard.entity.ProductEntitlement;
import com.furtim.entitleguard.repository.CustomerRepository;
import com.furtim.entitleguard.repository.CustomerSourceMapRepository;
import com.furtim.entitleguard.repository.DocumentsRepository;
import com.furtim.entitleguard.repository.EntitlementRepository;
import com.furtim.entitleguard.repository.OrderItemRepository;
import com.furtim.entitleguard.repository.OrderRepository;
import com.furtim.entitleguard.repository.ProductEntitlementRepository;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.utils.UserSessionUtil;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ProductService {

	private final ProductEntitlementRepository productEntitlementRepo;

	private final EntitlementRepository entitlementRepo;

	private final DocumentsRepository documentsRepo;

	private final OrderItemRepository orderItemRepo;

	private final OrderRepository orderRepo;

	private final CustomerRepository customerRepo;

	private final CustomerSourceMapRepository customerSourceMapRepo;

	private final RestTemplate restTemplate = new RestTemplate();

	private final ObjectMapper mapper = new ObjectMapper();

//	 public List<ProductEntitlement> getProductWithMetafields(String shopName, String apiVersion, String accessToken, String productId, OrderItem item) {
//
//		    String productUrl = String.format("https://%s/admin/api/%s/products/%s.json", shopName, apiVersion, productId);
//		    JsonNode productJson = getJsonResponse(productUrl, accessToken);
//		    log.info("productJson{}", productJson);
//
//		    String metafieldUrl = String.format("https://%s/admin/api/%s/products/%s/metafields.json", shopName, apiVersion, productId);
//		    JsonNode metafieldJson = getJsonResponse(metafieldUrl, accessToken);
//		    log.info("metafieldJson{}", metafieldJson);
//
//		    JsonNode productNode = productJson.get("product");
//		    item.setProductType(productNode.get("product_type").asText());
//		    orderItemRepo.save(item); 
//
//		    ProductEntitlement warrantyEntitlement = new ProductEntitlement();
//		    warrantyEntitlement.setOrderItem(item);
//		    warrantyEntitlement.setEntitlement(entitlementRepo.findByName("warranty"));
//
//		    ProductEntitlement returnEntitlement = new ProductEntitlement();
//		    returnEntitlement.setOrderItem(item);
//		    returnEntitlement.setEntitlement(entitlementRepo.findByName("return policy"));
//
//		    boolean hasWarrantyData = false;
//		    boolean hasReturnData = false;
//
//		    for (JsonNode field : metafieldJson.get("metafields")) {
//		        String key = field.get("key").asText();
//		        String value = field.get("value").asText();
//
//		        switch (key) {
//		            case "entitleguard_warranty":
//		                warrantyEntitlement.setWarrantyInMonths(Integer.parseInt(value));
//		                hasWarrantyData = true;
//		                break;
//
//		            case "entitleguard_warranty_description":
//		                warrantyEntitlement.setWarrantyDescription(value);
//		                hasWarrantyData = true;
//		                break;
//
//		            case "entitleguard_warranty_file":
//		                Documents warrantyDoc = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
//		                warrantyDoc.setType("warranty");
//		                documentsRepo.save(warrantyDoc);
//		                warrantyEntitlement.setWarrantyDocuments(warrantyDoc);
//		                hasWarrantyData = true;
//		                break;
//
//		            case "entitleguard_return_policy":
//		                returnEntitlement.setReturnDays(Integer.parseInt(value));
//		                hasReturnData = true;
//		                break;
//
//		            case "entitleguard_return_policy_des":
//		                returnEntitlement.setReturnPolicyDescription(value);
//		                hasReturnData = true;
//		                break;
//		                
//		            case "entitleguard_return_policy_file":
//		                Documents returnDoc = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
//		                returnDoc.setType("return_policy");
//		                documentsRepo.save(returnDoc);
//		                returnEntitlement.setReturnPolicyDocument(returnDoc);
//		                hasReturnData = true;
//		                break;
//		                
//		            case "entitleguard_installation_file":
//		                Documents manualDoc = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
//		                manualDoc.setType("Manual");
//		                documentsRepo.save(manualDoc);
//		                item.setProductManual(manualDoc);
//		                orderItemRepo.save(item);
//		                hasReturnData = true;
//		                break;
//		        }
//		    }
//
//		    List<ProductEntitlement> result = new ArrayList<>();
//
//		    if (hasWarrantyData) {
//		        productEntitlementRepo.save(warrantyEntitlement);
//		        result.add(warrantyEntitlement);
//		    }
//
//		    if (hasReturnData) {
//		        productEntitlementRepo.save(returnEntitlement);
//		        result.add(returnEntitlement);
//		    }
//
//		    return result;
//		}

	public List<ProductEntitlement> getProductWithMetafields(String shopName, String apiVersion, String accessToken,
			String productId, OrderItem item) {
		String productUrl = String.format("https://%s/admin/api/%s/products/%s.json", shopName, apiVersion, productId);
		JsonNode productJson = getJsonResponse(productUrl, accessToken);
		log.info("productJson: {}", productJson);

		String metafieldUrl = String.format("https://%s/admin/api/%s/products/%s/metafields.json", shopName, apiVersion,
				productId);
		JsonNode metafieldJson = getJsonResponse(metafieldUrl, accessToken);
		log.info("metafieldJson: {}", metafieldJson);
		boolean orderItemUpdated = false;

		JsonNode productNode = productJson.get("product");
		if (productNode != null && productNode.has("product_type") && !productNode.get("product_type").isNull()) {
			item.setProductType(productNode.get("product_type").asText());
			orderItemUpdated = true;
		}

		if (productNode != null && productNode.has("image") && productNode.get("image").has("src")) {
			String imageUrl = productNode.get("image").get("src").asText();
			item.setProductImageUrl(imageUrl);
			orderItemUpdated = true;
		}
		
		if (productNode != null && productNode.has("body_html") && !productNode.get("body_html").isNull()) {
			item.setProductDesc(productNode.get("body_html").asText());
			orderItemUpdated = true;
		}
		
		

		Map<String, ProductEntitlement> entitlementMap = new HashMap<>();
		List<String> entitlementTypes = List.of("warranty", "return_policy");

		for (String type : entitlementTypes) {
			ProductEntitlement ent = new ProductEntitlement();
			ent.setOrderItem(item);
			ent.setEntitlement(entitlementRepo.findByName(type));
			entitlementMap.put(type, ent);
		}

		JsonNode metafieldsNode = metafieldJson.get("metafields");
		if (metafieldsNode != null && metafieldsNode.isArray()) {
			for (JsonNode field : metafieldsNode) {
				String key = field.has("key") ? field.get("key").asText() : null;
				String value = field.has("value") ? field.get("value").asText() : null;

				if (key == null || value == null)
					continue;

				try {
					switch (key) {
					case "entitleguard_warranty":
					case "warranty_in_months": {
						ProductEntitlement warranty = entitlementMap.get("warranty");
						int months = Integer.parseInt(value);
						warranty.setEntitlementPeriodValue(months);
						warranty.setEntitlementPeriodType("months");
						warranty.setEntitlementExpiryDate(LocalDate.now().plusMonths(months));
						break;
					}

					case "entitleguard_warranty_description": {
						ProductEntitlement warranty = entitlementMap.get("warranty");
						warranty.setEntitlementPolicyDescription(value);
						break;
					}

					case "entitleguard_warranty_file": {
						ProductEntitlement warranty = entitlementMap.get("warranty");
						Documents doc = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
						doc.setOrderItem(item);
						doc.setType("warranty");
						documentsRepo.save(doc);
						warranty.setEntitlementDocuments(doc);
						break;
					}

					case "entitleguard_return_policy":
					case "return_days": {
						ProductEntitlement ret = entitlementMap.get("return_policy");
						int days = Integer.parseInt(value);
						ret.setEntitlementPeriodValue(days);
						ret.setEntitlementPeriodType("days");
						ret.setEntitlementExpiryDate(LocalDate.now().plusDays(days));
						break;
					}

					case "entitleguard_return_policy_des": {
						ProductEntitlement ret = entitlementMap.get("return_policy");
						ret.setEntitlementPolicyDescription(value);
						break;
					}

					case "entitleguard_return_policy_file": {
						ProductEntitlement ret = entitlementMap.get("return_policy");
						Documents doc = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
						doc.setOrderItem(item);
						doc.setType("return_policy");
						documentsRepo.save(doc);
						ret.setEntitlementDocuments(doc);
						break;
					}

					case "entitleguard_installation_file": {
						Documents manual = fetchDocumentDetailsFromFileReference(value, accessToken, shopName);
						manual.setOrderItem(item);
						manual.setType("Manual");
						documentsRepo.save(manual);
						item.setProductManual(manual);
						orderItemUpdated = true;
						break;
					}

					case "entitleguard_extra_file": {
						List<Documents> extraDocs = new ArrayList<>();
						if (value.startsWith("[") && value.endsWith("]")) {
							value = value.substring(1, value.length() - 1);
							String[] fileRefs = value.replace("\"", "").split(",");
							for (String fileRef : fileRefs) {
								Documents doc = fetchDocumentDetailsFromFileReference(fileRef.trim(), accessToken,
										shopName);
								doc.setType("extra");
								doc.setOrderItem(item);
								documentsRepo.save(doc);
								extraDocs.add(doc);
							}
							item.setExtraFiles(extraDocs);
							orderItemUpdated = true;
						}
						break;
					}
					}
				} catch (Exception ex) {
					log.warn("Failed processing metafield key: {} with value: {} due to: {}", key, value,
							ex.getMessage());
				}
			}
		}

		if (orderItemUpdated) {
			orderItemRepo.save(item);
		}

		List<ProductEntitlement> finalEntitlements = entitlementMap
				.values().stream().filter(ent -> ent.getEntitlementPeriodValue() != null
						|| ent.getEntitlementPolicyDescription() != null || ent.getEntitlementDocuments() != null)
				.collect(Collectors.toList());

		if (!finalEntitlements.isEmpty()) {
			productEntitlementRepo.saveAll(finalEntitlements);
		}

		return finalEntitlements;
	}

	private Documents fetchDocumentDetailsFromFileReference(String gid, String accessToken, String shop) {
		String graphqlUrl = "https://" + shop + "/admin/api/2024-01/graphql.json";

		String graphqlQuery = "{" + "  node(id: \"" + gid + "\") {" + "    ... on MediaImage {"
				+ "      mediaContentType" + "      image {" + "        originalSrc" + "        altText"
				+ "        width" + "        height" + "      }" + "    }" + "    ... on GenericFile {" + "      url"
				+ "      mimeType" + "    }" + "  }" + "}";

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Shopify-Access-Token", accessToken);

		Map<String, String> body = new HashMap<>();
		body.put("query", graphqlQuery);

		HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);
		ResponseEntity<String> response = restTemplate.exchange(graphqlUrl, HttpMethod.POST, entity, String.class);

		try {
			JsonNode root = mapper.readTree(response.getBody());
			log.info("root {}", root);
			JsonNode node = root.path("data").path("node");

			Documents doc = new Documents();

			doc.setDocumentSource(gid);

			if (node.has("image")) {

				doc.setLocation(node.path("image").path("originalSrc").asText());
				doc.setContentType("image");
			} else if (node.has("url")) {
				doc.setLocation(node.path("url").asText());
				doc.setContentType(node.path("mimeType").asText());
			} else {
				log.warn("Unknown document format for gid: {}", gid);
				doc.setLocation(null);
				doc.setContentType("unknown");
			}

			return doc;

		} catch (Exception e) {
			throw new RuntimeException("Failed to fetch document from file_reference", e);
		}
	}

	private JsonNode getJsonResponse(String url, String accessToken) {
		HttpHeaders headers = new HttpHeaders();
		headers.set("X-Shopify-Access-Token", accessToken);
		headers.setContentType(MediaType.APPLICATION_JSON);

		log.info("headers{}", headers);

		HttpEntity<String> entity = new HttpEntity<>(headers);
		log.info("entity{}", headers);
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
		log.info("response{}", response);
		try {
			return mapper.readTree(response.getBody());
		} catch (Exception e) {
			throw new RuntimeException("Failed to parse Shopify response", e);
		}
	}

	public DefaultListResponse getAllProductCategory() {
		try {
			String customerId = UserSessionUtil.getUserInfo().getId();
			Optional<Customer> cus = customerRepo.findOneById(customerId);
			Set<String> productCategoryName = new HashSet<>();
			if (cus.isPresent()) {
				List<CustomerSourceMap> customerSourceMaps = customerSourceMapRepo.findAllByIsActiveAndCustomer(true,
						cus.get());

				for (CustomerSourceMap csm : customerSourceMaps) {
					List<Orders> orders = orderRepo.findAllByIsActiveAndCustomerSourceMap(true, csm);
					for (Orders order : orders) {
						List<OrderItem> orderItems = orderItemRepo.findAllByOrder(order);
						for (OrderItem ot : orderItems) {
							productCategoryName.add(ot.getProductType());
						}
					}

				}
			}
			List<String> productCategories = new ArrayList<>(productCategoryName);
			return new DefaultListResponse(true, "Product categories fertch successfully", productCategories);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultListResponse(false, "Something went wrong during getting product categories ", null);
		}
	}

}
