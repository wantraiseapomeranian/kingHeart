package com.kh.shoppingmall.vo.kakaopay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//카카오페이 결제 승인에 필요한 데이터
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayApproveRequestVO {
	private String partnerOrderId;
	private String partnerUserId;
	private String tid;
	private String pgToken;
}