package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.UserInfo;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, String> {

	Optional<UserInfo> findOneByIsActiveAndEmail(boolean b, String email);

	Optional<UserInfo> findOneByEmailAndIsActive(String email, boolean b);

	Optional<UserInfo> findOneById(String id);

	@Query("SELECT u from UserInfo u where u.isActive = ?1 and u.builderOrganization.id = ?2 ")
	List<UserInfo> findAllByIsActiveAndBuilder(boolean b, String builderId);

}
