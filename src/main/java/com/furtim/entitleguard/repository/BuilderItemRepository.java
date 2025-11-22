package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BuilderItem;

@Repository
public interface BuilderItemRepository extends JpaRepository<BuilderItem, String> {

	Optional<BuilderItem> findOneById(String id);

	@Query("SELECT bt from BuilderItem bt where bt.isActive = ?1 and bt.builderOrganization.id = ?2 ")
	List<BuilderItem> findAllIsActiveAndSource(boolean b, String builderId);

	@Query("SELECT bt FROM BuilderItem bt WHERE bt.billOfMaterials.id = ?1 and bt.isActive = ?2 and bt.billOfMaterials.isActive = 'true' ")
	List<BuilderItem> findByBillOfMaterialsAndIsActive(String billId, boolean b);

}
