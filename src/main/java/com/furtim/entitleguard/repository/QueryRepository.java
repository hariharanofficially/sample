package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Query;

@Repository
public interface QueryRepository extends JpaRepository<Query, String> {

	Optional<Query> findOneById(String id);

	@org.springframework.data.jpa.repository.Query("SELECT q FROM Query q WHERE q.isActive = ?1 AND q.orderItem.order.customerSourceMap.customer.id = ?2 ")
	List<Query> findAllByIsActiveAndCustomer(boolean b, String customer);

}
