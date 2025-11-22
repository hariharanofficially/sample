package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.Orders;
import com.furtim.entitleguard.entity.Source;

@Repository
public interface CustomerSourceMapRepository extends JpaRepository<CustomerSourceMap, String>{

	Optional<CustomerSourceMap> findByCustomerIdAndSourceIdAndReferenceId(String id, String id2, String referenceId);

	@Query("SELECT csm FROM CustomerSourceMap csm  WHERE csm.isActive = ?1 and csm.customer = ?2 ")
	List<CustomerSourceMap> findAllByIsActiveAndCustomer(boolean b, Customer customer);

	Optional<CustomerSourceMap> findByCustomerAndSource(Customer customer, Source source);

}
