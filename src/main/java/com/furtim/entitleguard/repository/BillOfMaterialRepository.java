package com.furtim.entitleguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BillOfMaterials;

@Repository
public interface BillOfMaterialRepository extends JpaRepository<BillOfMaterials,String>{

	List<BillOfMaterials> findAllByIsActive(boolean b);

}
