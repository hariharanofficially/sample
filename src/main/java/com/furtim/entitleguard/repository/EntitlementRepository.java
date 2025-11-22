package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Entitlement;

@Repository
public interface EntitlementRepository extends JpaRepository<Entitlement, String> {

	Entitlement findByName(String string);

	Optional<Entitlement> findOneByName(String string);

}
