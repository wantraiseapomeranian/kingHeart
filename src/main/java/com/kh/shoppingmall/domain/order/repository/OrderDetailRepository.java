package com.kh.shoppingmall.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.kh.shoppingmall.domain.order.entity.OrderDetail;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
	
}