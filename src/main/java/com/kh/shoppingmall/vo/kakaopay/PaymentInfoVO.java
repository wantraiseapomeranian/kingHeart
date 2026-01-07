package com.kh.shoppingmall.vo.kakaopay;

import java.util.List;

import com.kh.shoppingmall.dto.PaymentDetailDto;
import com.kh.shoppingmall.dto.PaymentDto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentInfoVO {
	
	private PaymentDto paymentDto;
	private List<PaymentDetailDto> paymentDetailList;
	private KakaoPayOrderResponseVO responseVO;
}
