package com.kh.shoppingmall.vo.kakaopay;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class KakaoPayApproveResponseVO {
	private String aid;//요청 고유 번호
	private String tid;//결제 고유 번호
	private String cid;//가맹점 코드
	private String sid;//정기 결제 코드
	private String partnerOrderId;//가맹점 내의 주문번호
	private String partnerUserId;//가맹점 내의 주문자
	private String paymentMethodType;//카드 or 현금
	private KakaoPayAmountVO amount;
	private KakaoPayCardInfoVO cardInfo;
	private String itemName;//상품이름
	private String itemCode;//상품코드
	private Integer quantity;//상품 수량
	private LocalDateTime createdAt;//결제시작시각
	private LocalDateTime approvedAt;//결제승인시각
	private String payload;//결제에 대한 메모
}