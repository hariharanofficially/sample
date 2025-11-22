package com.furtim.entitleguard.configuration;

import java.util.Optional;
import java.util.function.Supplier;

import com.furtim.entitleguard.dto.CustomerDto;

public class UserSession {
	
	private static UserSession INSTANCE = new UserSession();

	private UserSession() {
	}

	private ThreadLocal<CustomerDto> currentUser = new InheritableThreadLocal<>();

	public void setCurrentUser(final CustomerDto userDetails) {
		currentUser.set(userDetails);
	}

	public Optional<CustomerDto> getCurrentUser() {
		return Optional.ofNullable(currentUser.get());
	}

	public static UserSession getInstance() {
		return INSTANCE;
	}

	public void clear() {

		currentUser.remove();
	}

	public CustomerDto getCurrentUserOrElseThrow(final Supplier<RuntimeException> exceptionSupplier) {
		Optional<CustomerDto> user = getCurrentUser();
		return user.orElseThrow(exceptionSupplier);
	}


}
