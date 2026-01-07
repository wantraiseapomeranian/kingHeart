package com.kh.shoppingmall.vo.kakaopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//결제 준비 요청에 필요한 데이터
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayReadyRequestVO {
	private String itemName;
	private int totalAmount;
	private String partnerOrderId;
	private String partnerUserId;
}