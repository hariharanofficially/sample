package com.furtim.entitleguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category,String>{

	@Query("SELECT c.name FROM Category c WHERE c.isActive = ?1 ")
	List<String> findByIsActive(boolean b);

	List<Category> findAllByIsActive(boolean b);

}
