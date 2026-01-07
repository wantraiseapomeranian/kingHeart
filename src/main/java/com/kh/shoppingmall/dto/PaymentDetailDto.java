package com.kh.shoppingmall.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDetailDto {
	
	private Long paymentDetailNo;
	private Long paymentDetailOrigin;
	private Long paymentDetailItemNo;
	private String paymentDetailItemName;
	private Integer paymentDetailItemPrice;
	private Integer paymentDetailQty;
	private String paymentDetailStatus;
	
	//구매 합계
	public Integer getPaymentDetailTotal() { 
		return paymentDetailItemPrice * paymentDetailQty;
	}
}
