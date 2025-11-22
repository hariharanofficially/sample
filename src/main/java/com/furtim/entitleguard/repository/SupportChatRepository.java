package com.furtim.entitleguard.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.SupportChat;

@Repository
public interface SupportChatRepository extends JpaRepository<SupportChat, String> {

	@Query("SELECT sc FROM SupportChat sc WHERE sc.orderItemId.id = :orderItemId AND sc.isActive = true ORDER BY sc.questionCreatedAt ASC")
	List<SupportChat> findAllByOrderItemId(String orderItemId);

	@Query("SELECT sc FROM SupportChat sc WHERE sc.orderItemId.order.customerSourceMap.customer.id = :user AND sc.isActive = true ORDER BY sc.questionCreatedAt ASC")
	List<SupportChat> findAllByCustomer(String user);

}
