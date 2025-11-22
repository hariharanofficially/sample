package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Status;

@Repository
public interface StatusRepository extends JpaRepository<Status, String> {

	Status findOneByModuleAndName(String string, String string2);

	Optional<Status> findOneById(String statusId);

}
