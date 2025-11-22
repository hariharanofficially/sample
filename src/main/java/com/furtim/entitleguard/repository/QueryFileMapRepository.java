

package com.furtim.entitleguard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.QueryFileMap;

@Repository
public interface QueryFileMapRepository extends JpaRepository<QueryFileMap, String> {

}
