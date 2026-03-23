package com.kh.shoppingmall.domain.cart.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kh.shoppingmall.domain.cart.entity.Cart;
import java.util.List;


public interface CartRepository extends JpaRepository<Cart, Integer>{
	
	//카트 목록 가져오기
	List<Cart> findByMemberId(String memberId);
	
	//장바구니 중복 조회
	Cart findByMemberIdAndProductNoAndOptionNo(String memberId, Integer productNo, Integer optionNo);
	
	//장바구니 전체 비우기
	void deleteByMemberId(String memberId);
	
}
