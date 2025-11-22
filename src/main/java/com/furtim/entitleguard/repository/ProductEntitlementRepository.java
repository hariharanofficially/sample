package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Entitlement;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.ProductEntitlement;

@Repository
public interface ProductEntitlementRepository extends JpaRepository<ProductEntitlement, String> {

	 Optional<ProductEntitlement> findOneByOrderItemAndEntitlement(OrderItem ot, Entitlement entitlement);

	List<ProductEntitlement> findAllByOrderItemId(String id);


}
