package com.furtim.entitleguard.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.furtim.entitleguard.entity.ShopToken;
import com.furtim.entitleguard.entity.Source;
import com.furtim.entitleguard.repository.ShopTokenRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ShopInstallationService {
    
    private final ShopTokenRepository shopTokenRepo;

    public void saveShopToken(String shop, String globalAccessToken, Source source) {
        try {
            Optional<ShopToken> st = shopTokenRepo.findOneByShop(shop);
            ShopToken shopToken;

            if (st.isPresent()) {
              
                shopToken = st.get();
                shopToken.setSource(source);
                shopToken.setAccessToken(globalAccessToken);
                log.info("Updating token for existing shop: {}", shop);
            } else {
             
                shopToken = new ShopToken();
                shopToken.setShop(shop);
                shopToken.setSource(source);
                shopToken.setAccessToken(globalAccessToken);
                log.info("Creating new token entry for shop: {}", shop);
            }

            shopTokenRepo.save(shopToken);

        } catch (Exception e) {
            log.error("Failed to save token for shop {}: {}", shop, e.getMessage(), e);
        }
    }
	


}
