package com.furtim.entitleguard.controller;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.view.RedirectView;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.furtim.entitleguard.entity.ShopToken;
import com.furtim.entitleguard.entity.Source;
import com.furtim.entitleguard.repository.ShopTokenRepository;
import com.furtim.entitleguard.repository.SourceRepository;
import com.furtim.entitleguard.response.ApiResponse;
import com.furtim.entitleguard.service.OrderService;
import com.furtim.entitleguard.service.ShopInstallationService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/shopify")
public class ShopAuthController {

	private final ShopInstallationService shopService;

	private final OrderService orderService;
	
	private final ShopTokenRepository shopTokenRepo;
	
	private final SourceRepository sourceRepo;
	
	

	@Value("${shopify.app.clientId}")
	private String clientId;

	@Value("${shopify.app.clientSecret}")
	private String clientSecret;

	@Value("${shopify.app.scopes}")
	private String scopes;

	@Value("${apiUrl}")
	private String apiUrl;

	private String globalAccessToken = "";

	public ShopAuthController(ShopInstallationService shopService, OrderService orderService,
			ShopTokenRepository shopTokenRepository,SourceRepository sourceRepo) {
		this.shopService = shopService;
		this.orderService = orderService;
		this.sourceRepo = sourceRepo;
		this.shopTokenRepo = shopTokenRepository;
	}

	private final RestTemplate restTemplate = new RestTemplate();

	@GetMapping("/init")
	public RedirectView init(@RequestParam Map<String, String> query) {
		log.info("query :{}", query);
		String shop = query.get("shop");

		String redirectUrl = String.format(
				"https://%s/admin/oauth/authorize?client_id=%s&scope=%s&redirect_uri=%s/shopify/redirect&state=&grant_options[]=",
				shop, clientId, scopes, apiUrl);
		log.info("redirectUrl :{}", redirectUrl);

		return new RedirectView(redirectUrl);
	}

	@GetMapping("/redirect")
	public RedirectView oauthRedirect(@RequestParam Map<String, String> query) {
		log.info("query :{}", query);
		String shop = query.get("shop");
		String code = query.get("code");

		String tokenUrl = String.format("https://%s/admin/oauth/access_token", shop);

		Map<String, String> body = new HashMap<>();
		body.put("client_id", clientId);
		body.put("client_secret", clientSecret);
		body.put("code", code);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);

		HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);
		log.info("request :{}", request);
		ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, request, Map.class);
		log.info("response :{}", response);

		if (response.getStatusCode() == HttpStatus.OK) {
			  Source source;
			  Optional<Source> existing = sourceRepo.findOneByCode("shopify");
			    if (existing.isEmpty()) {
			        source = new Source();
			        source.setName("Shopify");
			        source.setCode("shopify");
			        source = sourceRepo.save(source);
			    } else {
			        source = existing.get();
			    }
			globalAccessToken = response.getBody().get("access_token").toString();
			shopService.saveShopToken(shop, globalAccessToken,source);

			createProductWarrantyMetafields(shop, globalAccessToken);
			
			registerOrderCancelledWebhook(shop, globalAccessToken);
		}
		log.info("globalAccessToken :{}", globalAccessToken);
		String redirectToApp = String.format("https://%s/admin/settings/checkout", shop, shop);
		log.info("redirectToApp :{}", redirectToApp);
		return new RedirectView(redirectToApp);
	}

	private void registerOrderCancelledWebhook(String shop, String token) {
	    String webhookUrl = String.format("https://%s/admin/api/2023-04/webhooks.json", shop);

	    HttpHeaders headers = new HttpHeaders();
	    headers.setContentType(MediaType.APPLICATION_JSON);
	    headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
	    headers.set("X-Shopify-Access-Token", token);

	    Map<String, Object> webhook = new HashMap<>();
	    webhook.put("topic", "orders/cancelled");
	    webhook.put("address", "https://app2.entitleguard.com/shopify/order-cancelled");
	    webhook.put("format", "json");

	    Map<String, Object> body = new HashMap<>();
	    body.put("webhook", webhook);

	    HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

	    try {
	        ResponseEntity<String> response = restTemplate.postForEntity(webhookUrl, request, String.class);
	        log.info("Webhook registration response: {}", response);
	    } catch (Exception e) {
	        log.error("Failed to register webhook: ", e);
	    }
	}
	
	@PostMapping("/order-cancelled")
	public ResponseEntity<String> handleOrderCancelled(@RequestBody Map<String, Object> payload) {
	    log.info("Received Shopify Order Cancelled Webhook: {}", payload);

	    try {
	        Object id = payload.get("id");
	        log.info("order deleted api process");
	        if (id != null) {
	            String orderId = String.valueOf(id);
	            ApiResponse response = orderService.deleteOrderById(orderId);

	            if (response.getSuccess()) {
	            	log.info("order deleted");
	                return ResponseEntity.ok("Order deleted");
	            } else {
	            	log.info("Order ID not found {}", id);
	                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Order ID not found");
	            }
	        } else {
	        	log.info("Invalid payload - missing 'id'");
	            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload - missing 'id'");
	        }

	    } catch (Exception e) {
	        log.error("Error processing webhook", e);
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing order cancellation");
	    }
	}



	private void createProductWarrantyMetafields(String shop, String globalAccessToken2) {
		String graphqlUrl = String.format("https://%s/admin/api/2025-04/graphql.json", shop);

		String graphqlQuery = """
				    mutation CreateTwoDefinitions(
				        $def1: MetafieldDefinitionInput!,
				        $def2: MetafieldDefinitionInput!,
				        $def3: MetafieldDefinitionInput!,
				        $def4: MetafieldDefinitionInput!,
				        $def5: MetafieldDefinitionInput!,
				        $def6: MetafieldDefinitionInput!,
				        $def7: MetafieldDefinitionInput!,
				        $def8: MetafieldDefinitionInput!
				    ) {
				        definitionOne: metafieldDefinitionCreate(definition: $def1) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionTwo: metafieldDefinitionCreate(definition: $def2) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionThree: metafieldDefinitionCreate(definition: $def3) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionFour: metafieldDefinitionCreate(definition: $def4) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionFive: metafieldDefinitionCreate(definition: $def5) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionSix: metafieldDefinitionCreate(definition: $def6) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }
				        definitionSeven: metafieldDefinitionCreate(definition: $def7) {
				            createdDefinition { id name }
				            userErrors { field message code }
				        }

				         definitionEight: metafieldDefinitionCreate(definition: $def8) {
				          createdDefinition { id name }
				          userErrors { field message code }
				      }

				    }
				""";

		Map<String, Object> variables = new HashMap<>();
		variables.put("def1",
				Map.of("name", "Warranty Period Description", "namespace", "custom", "key",
						"entitleguard_warranty_description", "type", "rich_text_field", "pin", true, "description",
						"Describe your product warranty information", "ownerType", "PRODUCT"));
		variables.put("def2",
				Map.of("name", "Warranty Period in months", "namespace", "custom", "key", "entitleguard_warranty",
						"type", "number_integer", "pin", true, "description",
						"The product's warranty duration in months", "ownerType", "PRODUCT"));
		variables.put("def3",
				Map.of("name", "Warranty File", "namespace", "custom", "key", "entitleguard_warranty_file", "type",
						"file_reference", "pin", true, "description", "Upload your warranty file", "ownerType",
						"PRODUCT"));
		variables.put("def4",
				Map.of("name", "Return Policy Description", "namespace", "custom", "key",
						"entitleguard_return_policy_des", "type", "rich_text_field", "pin", true, "description",
						"Describe your return policy", "ownerType", "PRODUCT"));
		variables.put("def5",
				Map.of("name", "Return Policy in days", "namespace", "custom", "key", "entitleguard_return_policy",
						"type", "number_integer", "pin", true, "description", "The product's return policy in days",
						"ownerType", "PRODUCT"));
		variables.put("def6",
				Map.of("name", "Return Policy File", "namespace", "custom", "key", "entitleguard_return_policy_file",
						"type", "file_reference", "pin", true, "description", "Upload your return policy file",
						"ownerType", "PRODUCT"));
		variables.put("def7",
				Map.of("name", "Product Installation File", "namespace", "custom", "key",
						"entitleguard_installation_file", "type", "file_reference", "pin", true, "description",
						"Upload your product installation file", "ownerType", "PRODUCT"));

		variables.put("def8",
				Map.of("name", "Product Extra Files", "namespace", "custom", "key", "entitleguard_extra_files", "type",
						"list.file_reference", "pin", true, "description", "Upload your product extra file",
						"ownerType", "PRODUCT"));

		Map<String, Object> graphqlBody = new HashMap<>();
		graphqlBody.put("query", graphqlQuery);
		graphqlBody.put("variables", variables);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("X-Shopify-Access-Token", globalAccessToken2);
		headers.setAccept(List.of(MediaType.APPLICATION_JSON));

		HttpEntity<Map<String, Object>> request = new HttpEntity<>(graphqlBody, headers);

		try {
			ResponseEntity<String> response = restTemplate.postForEntity(graphqlUrl, request, String.class);
			log.info("Metafield Creation Response: {}", response.getBody());
		} catch (Exception e) {
			log.error("Error while creating metafields", e);
		}
	}

	@GetMapping("/getorder")
	public ResponseEntity<String> getOrder(@RequestParam Map<String, String> query) {
		try {

			String shop = query.get("shop");
			String referenceId = query.get("referenceId");

			Optional<ShopToken> shopTokenOpt = shopTokenRepo.findByShop(shop);
			if (shopTokenOpt.isEmpty()) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Access token not found for shop: " + shop);
			}
			String accessToken = shopTokenOpt.get().getAccessToken();
			log.info("Access token for {}: {}", shop, accessToken);

			String orderUrl = String.format("https://%s/admin/api/2025-04/orders.json?checkout_token=%s", shop,
					referenceId);
			log.info("orderUrl :{}", orderUrl);
			HttpHeaders headers = new HttpHeaders();
			headers.set("X-Shopify-Access-Token", accessToken);

			HttpEntity<Void> request = new HttpEntity<>(headers);
			ResponseEntity<String> response = restTemplate.exchange(orderUrl, HttpMethod.GET, request, String.class);
			log.info("response :{}", response);
			ObjectMapper mapper = new ObjectMapper();
			JsonNode root = mapper.readTree(response.getBody());

			JsonNode orders = root.get("orders");
			if (orders != null && orders.isArray()) {
				for (JsonNode orderJson : orders) {
					orderService.saveShopifyOrder(orderJson, shop, accessToken,shopTokenOpt.get());
				}
			}
			log.info("orders :{}", orders);

			return ResponseEntity.ok(response.getBody());
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body("Failed to process Shopify order: " + e.getMessage());
		}
	}

//	    @GetMapping("/product/details")
//	    public ProductEntitlement getProductWithWarrantyAndReturnPolicy(
//	            @RequestParam String shopName,
//	            @RequestParam String apiVersion,
//	            @RequestParam String accessToken,
//	            @RequestParam String productId
//	    ) {
//	        return productService.getProductWithMetafields(shopName, apiVersion, accessToken, productId);
//	    }
	
//    public void cancelAndDeleteOrder(String orderId) throws IOException {
//        String gidOrderId = "gid://shopify/Order/" + orderId;
//
//        String query = """
//            mutation OrderCancel($orderId: ID!, $notifyCustomer: Boolean, $refundMethod: OrderCancelRefundMethodInput!, $restock: Boolean!, $reason: OrderCancelReason!, $staffNote: String) {
//              orderCancel(orderId: $orderId, notifyCustomer: $notifyCustomer, refundMethod: $refundMethod, restock: $restock, reason: $reason, staffNote: $staffNote) {
//                job {
//                  id
//                  done
//                }
//                orderCancelUserErrors {
//                  field
//                  message
//                  code
//                }
//                userErrors {
//                  field
//                  message
//                }
//              }
//            }
//        """;
//
//        JSONObject variables = new JSONObject();
//        variables.put("orderId", gidOrderId);
//        variables.put("notifyCustomer", true);
//        variables.put("restock", true);
//        variables.put("reason", "CUSTOMER");
//        variables.put("staffNote", "Wrong size. Customer reached out saying they already re-purchased the correct size.");
//
//        JSONObject refundMethod = new JSONObject();
//        refundMethod.put("originalPaymentMethodsRefund", true);
//        variables.put("refundMethod", refundMethod);
//
//        JSONObject payload = new JSONObject();
//        payload.put("query", query);
//        payload.put("variables", variables);
//
//        OkHttpClient client = new OkHttpClient();
//
//        RequestBody body = RequestBody.create(payload.toString(), MediaType.get("application/json; charset=utf-8"));
//
//        Request request = new Request.Builder()
//            .url(GRAPHQL_ENDPOINT)
//            .addHeader("X-Shopify-Access-Token", ACCESS_TOKEN)
//            .post(body)
//            .build();
//
//        try (Response response = client.newCall(request).execute()) {
//            if (!response.isSuccessful()) {
//                System.err.println("Shopify cancel failed: " + response.code());
//                return;
//            }
//
//            String responseBody = response.body().string();
//            System.out.println("Shopify Response: " + responseBody);
//
//            // parse response to check for errors (simplified)
//            JSONObject json = new JSONObject(responseBody);
//            JSONObject data = json.optJSONObject("data");
//            if (data != null && data.has("orderCancel")) {
//                JSONObject cancelResult = data.getJSONObject("orderCancel");
//
//                if (cancelResult.getJSONArray("orderCancelUserErrors").isEmpty() &&
//                    cancelResult.getJSONArray("userErrors").isEmpty()) {
//                    // ✅ No errors: proceed to delete in database                    ApiResponse dbResponse = orderService.deleteOrderById(orderId);
//                    System.out.println("DB Delete: " + dbResponse.getMessage());
//                } else {
//                    System.err.println("Shopify cancel errors occurred. DB delete skipped.");
//                }
//            }
//        }
//    }
//

}
