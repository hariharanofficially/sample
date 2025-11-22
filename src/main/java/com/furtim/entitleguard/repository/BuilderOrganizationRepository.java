package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.BuilderOrganization;

@Repository
public interface BuilderOrganizationRepository extends JpaRepository<BuilderOrganization, String> {

	Optional<BuilderOrganization> findOneById(String builderOrganizationId);

	Optional<BuilderOrganization> findOneByIdAndIsActive(String builderId, boolean b);


}
