package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BuilderCustomerItemFiles;
import com.furtim.entitleguard.entity.BuilderCustomerItemMap;

@Repository
public interface BuilderCustomerItemFilesRepository extends JpaRepository<BuilderCustomerItemFiles, String> {

	List<BuilderCustomerItemFiles> findAllByBuilderItemmapAndIsActive(BuilderCustomerItemMap map, boolean b);

	Optional<BuilderCustomerItemFiles> findOneByIdAndIsActive(String id, boolean b);

}
