package com.furtim.entitleguard.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.furtim.entitleguard.dto.SupportChatDto;
import com.furtim.entitleguard.response.DefaultListResponse;
import com.furtim.entitleguard.service.SearchService;


@RestController
@RequestMapping("/api")
public class SearchController {
	

	@Autowired
	private SearchService searchService;
	
	@PostMapping("/chat")
    public DefaultListResponse chat(@RequestBody SupportChatDto supportChatDto) {
        String orderItemId = supportChatDto.getOrderItemId();
        String userInput = supportChatDto.getUserInput();

        if (orderItemId == null || userInput == null || userInput.trim().isEmpty()) {
            return new DefaultListResponse(false, "Missing 'order_item_id' or 'user_input'", null);
        }

        return searchService.getSupportResponse(orderItemId, userInput);
    }

	@GetMapping("/chatsupport/history")
	public DefaultListResponse chatSupport(@RequestParam(name = "orderItemId") String orderItemId) {
		return searchService.getChatSupportHis(orderItemId);
	}
	
	@GetMapping("/chatsupport/item/history")
	public DefaultListResponse chatSupportItemHistory() {
		return searchService.chatSupportItemHistory();
	}

}
