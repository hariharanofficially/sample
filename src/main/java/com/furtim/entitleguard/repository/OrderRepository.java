package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.Orders;

@Repository
public interface OrderRepository extends JpaRepository<Orders, String> {

	@Query("SELECT o FROM Orders o WHERE o.isActive = ?1 AND o.customerSourceMap = ?2 ")
	List<Orders> findAllByIsActiveAndCustomerSourceMap(boolean b, CustomerSourceMap csm);

	boolean existsByOrderId(String orderId);

	Optional<Orders> findOneByOrderId(String orderId);

	@Query("SELECT o FROM Orders o WHERE o.isActive = ?1 AND o.customerSourceMap.customer = ?2 ")
	List<Orders> findAllByIsActiveAndCustomer(boolean b, Customer customer);

	@Query("SELECT o FROM Orders o WHERE o.isActive = ?1 AND o.customerSourceMap.customer = ?2 and o.type = ?3 ")
	List<Orders> findAllByIsActiveAndCustomerAndType(boolean b, Customer customer, String type);

	Optional<Orders> findOneById(String id);

}
