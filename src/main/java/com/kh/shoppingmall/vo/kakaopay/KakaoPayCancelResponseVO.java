package com.kh.shoppingmall.vo.kakaopay;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoPayCancelResponseVO {
	
	private String aid; //요청 고유번호
 	private String tid; //
	private String cid; //
	private String status;//주문 상태
	private String partnerOrderId;//주문번호
	private String partnerUserId; //주문자
	private String paymentMethodType;//결제수단
	private KakaoPayAmountVO amount;//결제금액
	private KakaoPayAmountVO approvedCancelAmount; //이번에 취소된 금액
	private KakaoPayAmountVO canceledAmount; //누적 취소된 금액
	private KakaoPayAmountVO cancelAvailableAmount; //남은 취소 가능 금액
	private String itemName; //상품명
 	private String itemCode; //상품코드
	private Integer quantity; //상품수량
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime createdAt; //결제 요청 시각
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime approvedAt; //결제 승인 시각
	@JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ss")
	private LocalDateTime calceledAt; //결제 취소 시각
	private String payload; //결제 추가 정보(메모)
}




