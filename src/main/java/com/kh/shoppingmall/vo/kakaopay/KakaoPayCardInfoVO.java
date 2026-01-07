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
public class KakaoPayCardInfoVO {
	private String kakaopayPurchaseCorp;//카드 매입사명
	private String kakaopayPurchaseCorpCode;//카드 매입사코드
	private String kakaopayIssuerCorp;//카드 발급사명
	private String kakaopayIssuerCorpCode;//카드 발급사 코드
	private String bin;//카드 BIN
	private String cardType;//카드 유형
	private String installMonth;//할부 개월 수
	private String approvedId;//카드사 승인번호
	private String cardMid;//카드사 가맹점번호
	private String interestFreeInstall;//무이자할부 여부(Y/N)
	private String cardItemCode;//카드 상품코드
}