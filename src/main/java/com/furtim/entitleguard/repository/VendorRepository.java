package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Vendor;

@Repository
public interface VendorRepository extends JpaRepository<Vendor, String> {

	Optional<Vendor> findOneById(String id);

	@Query("SELECT v from Vendor v where v.isActive = ?1 and v.builderOrganization.id = ?2 ")
	List<Vendor> findAllByIsActiveAndBuilderOrganization(boolean b, String builderId);

}
