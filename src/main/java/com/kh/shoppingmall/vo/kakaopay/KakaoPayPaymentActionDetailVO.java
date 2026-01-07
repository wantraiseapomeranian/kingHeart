package com.kh.shoppingmall.vo.kakaopay;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoPayPaymentActionDetailVO {
	
	private String aid;//요청 고유 번호
	private LocalDateTime approvedAt;//거래 시각
	private Integer amount;//결제 혹은 취소 금액
	private Integer pointAmount;//결제 혹은 취소에 대한 포인트 금액
	private Integer discountAmount;//할인 금액
	private Integer greenDeposit;//컵 보증금
	private String paymentActionType;//결제 타입
	private String payload;//추가 전달값
}
