package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, String> {

	Optional<Customer> findOneByEmailAndIsActive(String username, boolean b);

	Optional<Customer> findByEmailAndIsActive(String email, boolean b);

	Optional<Customer> findByContactAndIsActive(String phone, boolean b);

	Customer findByEmail(String email);

	Customer findByContact(String phone);

	Optional<Customer> findByEmailAndIsActiveAndIsRegistered(String email, boolean b, boolean c);

	Optional<Customer> findByContactAndIsActiveAndIsRegistered(String phone, boolean b, boolean c);

	Optional<Customer> findOneById(String id);


}
