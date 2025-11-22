package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.furtim.entitleguard.entity.Catlog;

public interface CatlogRepository extends JpaRepository<Catlog, String> {

	List<Catlog> findAllByIsActive(boolean b);

	Optional<Catlog> findOneById(String catlogId);

}
