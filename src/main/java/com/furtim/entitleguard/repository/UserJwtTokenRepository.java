package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.UserJwtToken;

@Repository
public interface UserJwtTokenRepository extends JpaRepository<UserJwtToken, String> {

	Optional<UserJwtToken> findOneByJwtAndLogged(String token, String string);

}
