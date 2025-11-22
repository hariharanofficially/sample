package com.furtim.entitleguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Documents;

@Repository
public interface DocumentsRepository extends JpaRepository<Documents, String> {

	List<Documents> findAllByOrderItemId(String id);
	

}
