package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Address;

@Repository
public interface AddressRepository extends JpaRepository<Address, String> {

	Optional<Address> findOneById(String id);

}
