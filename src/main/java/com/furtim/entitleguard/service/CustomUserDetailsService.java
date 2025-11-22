package com.furtim.entitleguard.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.furtim.entitleguard.entity.Customer;
import com.furtim.entitleguard.repository.CustomerRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
	
	private final CustomerRepository customerRepo;
	
	public CustomUserDetailsService(CustomerRepository customerRepo) {
		this.customerRepo = customerRepo;
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
	    Optional<Customer> useremail = customerRepo.findOneByEmailAndIsActive(username, true);

	    if (useremail.isPresent()) {
	        Customer customer = useremail.get();
	        String otp = customer.getOtp() != null ? customer.getOtp() : "";
	        return new User(customer.getEmail(), otp, new ArrayList<>());
	    }

	    Optional<Customer> userContact = customerRepo.findByContactAndIsActive(username, true);

	    if (userContact.isPresent()) {
	        Customer customer = userContact.get();
	        String otp = customer.getOtp() != null ? customer.getOtp() : "";
	        return new User(customer.getContact(), otp, new ArrayList<>());
	    }

	    throw new UsernameNotFoundException("User not found with email or contact: " + username);
	}


}
