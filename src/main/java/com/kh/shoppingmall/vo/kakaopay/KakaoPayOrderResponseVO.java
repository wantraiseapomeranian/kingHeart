package com.kh.shoppingmall.vo.kakaopay;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//카카오페이에서 전달되는 데이터를 수신하는 클래스
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoPayOrderResponseVO {
	private String cid; //결제 고유번호
	private String tid; //가맹점 코드
	private String status; //결제 상태
	private String partnerOrderId; //주문번호
	private String partnerUserId; //주문자
	private String paymentMethodType; //결제 수단
	private KakaoPayAmountVO amount;//결제 금액
	private KakaoPayAmountVO cancelAmount;//결제 취소된 금액
	private KakaoPayAmountVO cancelAvailableAmount;//결제 취소 가능 금액
	
	private String itemName; //상품명
	private String itemCode; //상품코드
	private Integer quantity; //상품 수량
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime createdAt;//시작시각
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime approvedAt;//승인시각
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime canceledAt;//취소시각
	private KakaoPaySelectedCardInfoVO selectedCardInfo; //결제 카드 정보
	private List<KakaoPayPaymentActionDetailVO> paymentActionDetails; //결제 및 취소 상세 내역
}





