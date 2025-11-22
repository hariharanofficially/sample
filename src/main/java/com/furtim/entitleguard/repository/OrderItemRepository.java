package com.furtim.entitleguard.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.furtim.entitleguard.entity.CustomerSourceMap;
import com.furtim.entitleguard.entity.OrderItem;
import com.furtim.entitleguard.entity.Orders;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

	List<OrderItem> findAllByOrder(Orders order);

	List<OrderItem> findAllByOrderAndProductType(Orders order, String productType);

	@Query("SELECT ot FROM OrderItem ot WHERE " +
		       "(LOWER(ot.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR LOWER(ot.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR str(ot.order.orderId) LIKE CONCAT('%', :search, '%')) " +
		       "AND (:productType IS NULL OR ot.productType = :productType) " +
		       "AND ot.order.customerSourceMap IN :customerSourceMaps")
		List<OrderItem> findAllBySearch(String search, List<CustomerSourceMap> customerSourceMaps, String productType);

	@Query("SELECT ot FROM OrderItem ot WHERE " +
		       "(LOWER(ot.productName) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR LOWER(ot.brand) LIKE LOWER(CONCAT('%', :search, '%')) " +
		       "OR str(ot.order.orderId) LIKE CONCAT('%', :search, '%')) " +
		       "AND ot.order.customerSourceMap IN :customerSourceMaps")
	List<OrderItem> findAllProductTypeBySearch(String search, List<CustomerSourceMap> customerSourceMaps);

	@Query("SELECT COUNT(ot.id) FROM OrderItem ot WHERE ot.order = ?1")
	Integer countByOrder(Orders order);

	void deleteByOrder(Optional<Orders> order);

	Optional<OrderItem> findOneById(String orderItemId);



}
