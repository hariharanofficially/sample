package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BuilderCustomer;
import com.furtim.entitleguard.entity.Status;

@Repository
public interface BuilderCustomerRepository extends JpaRepository<BuilderCustomer, String> {

	Optional<BuilderCustomer> findOneById(String id);

	@Query("SELECT COUNT(bc.id) from BuilderCustomer bc Where bc.isActive = ?1 and bc.builderOrganization.id = ?2 ")
	Integer countByIsActiveAndBuilder(boolean b, String builderId);

	@Query("SELECT COUNT(bc.id) from BuilderCustomer bc Where bc.isActive = ?1 and bc.builderOrganization.id = ?2 And bc.status = ?3 ")
	Integer countByIsActiveAndBuilderAndStatus(boolean b, String builderId, Status entitlement);

	@Query("SELECT bc from BuilderCustomer bc Where bc.isActive = ?1 and bc.builderOrganization.id = ?2 order by status.name ASC ")
	List<BuilderCustomer> findAllByIsActiveAndBuilderId(boolean b, String builderId);
	
	

}
