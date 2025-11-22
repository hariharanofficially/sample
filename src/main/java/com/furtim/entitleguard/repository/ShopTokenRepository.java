package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.ShopToken;

@Repository
public interface ShopTokenRepository extends JpaRepository<ShopToken, String> {

	Optional<ShopToken> findOneByShop(String shop);

	Optional<ShopToken> findByShop(String shop);

}
