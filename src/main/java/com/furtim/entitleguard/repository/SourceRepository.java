package com.furtim.entitleguard.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.Source;

@Repository
public interface SourceRepository extends JpaRepository<Source, String> {

	Source findByCode(String string);

	Optional<Source> findOneByCode(String string);

	Optional<Source> findOneById(String sourceId);

}
