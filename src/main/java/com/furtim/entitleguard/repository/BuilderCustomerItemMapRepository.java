package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BuilderCustomer;
import com.furtim.entitleguard.entity.BuilderCustomerItemMap;
import com.furtim.entitleguard.entity.BuilderItem;

@Repository
public interface BuilderCustomerItemMapRepository extends JpaRepository<BuilderCustomerItemMap, String> {

	List<BuilderCustomerItemMap> findAllByBuilderCustomer(BuilderCustomer customer);

    @Query("SELECT m FROM BuilderCustomerItemMap m WHERE m.builderCustomer.id = :customerId AND m.isActive = true ")
	List<BuilderCustomerItemMap> findAllByByCustomerId(String customerId);

	Optional<BuilderCustomerItemMap> findOneById(String id);

	List<BuilderCustomerItemMap> findByBuilderCustomerId(String builderCustomerId);

	List<BuilderCustomerItemMap> findAllByBuilderItemAndIsActive(BuilderItem builderItem, boolean b);

    @Query("SELECT m FROM BuilderCustomerItemMap m WHERE m.builderCustomer.id = :customerId AND m.isActive = :string ")
	List<BuilderCustomerItemMap> findAllByByCustomerIdAndIsActive(String customerId, boolean string);

    @Query("SELECT COUNT(DISTINCT m.builderItem.category) FROM BuilderCustomerItemMap m WHERE m.builderCustomer.id = :customerId AND m.isActive = :isActive")
	Integer countByCustomerIdAndIsActive(String customerId, boolean isActive);

}
