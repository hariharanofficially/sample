package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.UserInfo;
import com.furtim.entitleguard.entity.UserPassword;

@Repository
public interface UserPasswordRepository extends JpaRepository<UserPassword, String> {

	Optional<UserPassword> findOneByUserInfoAndIsActive(UserInfo userInfo, boolean b);

}
