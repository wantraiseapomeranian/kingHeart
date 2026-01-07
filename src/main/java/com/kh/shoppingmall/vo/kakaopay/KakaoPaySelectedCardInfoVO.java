package com.kh.shoppingmall.vo.kakaopay;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class KakaoPaySelectedCardInfoVO {
	
	private String cardBin;//카드 BIN
	private Integer installMonth;//할부 개월 수
	private String installType;//할부 유형
	private String cardCorpName;//카드사 정보
	private String interestFreeInstall;//무이자할부 여부(Y/N)
}
