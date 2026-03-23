package com.kh.shoppingmall.domain.cart.entity;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cart_seq_gen")
	@SequenceGenerator(name = "cart_seq_gen", sequenceName = "cart_seq", allocationSize = 1)
	@Column(name = "cart_no")
	private Integer cartNo;
	
	@Column(name = "member_id")
	private String memberId;
	
	@Column(name = "product_no")
	private Integer productNo;
	
	@Column(name = "option_no")
	private Integer optionNo;
	
	@Column(name = "cart_amount")
	private int cartAmount;
	
	@CreationTimestamp
	@Column(name = "cart_created_at", updatable = false)
	private Timestamp cartCreatedAt;
	
	@Builder
	public Cart(String memberId, Integer productNo, Integer optionNo, int cartAmount, Timestamp cartCreatedAt) {
		this.memberId = memberId;
		this.productNo = productNo;
		this.optionNo = optionNo;
		this.cartAmount = cartAmount;
	}
	
	//수량 변경을 위한 메소드
	public void updateAmount(int cartAmount) {
		this.cartAmount = cartAmount;
	}
	
}
