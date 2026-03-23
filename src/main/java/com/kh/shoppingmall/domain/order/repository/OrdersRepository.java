package com.kh.shoppingmall.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.shoppingmall.domain.order.entity.Orders;

public interface OrdersRepository extends JpaRepository<Orders, Integer>{
	
	
}
