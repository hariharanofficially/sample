package com.furtim.entitleguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Files;

@Repository
public interface FilesRepository extends JpaRepository<Files, String> {

	Files findOneById(String id);

}
