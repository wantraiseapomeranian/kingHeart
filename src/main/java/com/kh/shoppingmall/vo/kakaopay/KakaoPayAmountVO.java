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
public class KakaoPayAmountVO {
	private Integer total;//전체 금액
	private Integer taxFree;//비과세액
	private Integer vat;//부가세액
	private Integer point;//포인트 사용금액
	private Integer discount;//할인 금액
	private Integer greenDeposit;//컵 보증금
}