package com.kh.shoppingmall.vo.kakaopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//카카오페이에서 준비요청에 대해 반환하는 응답 중 필요한 것을 저장하기 위한 클래스
//→ 중요한건 "모두" 받는게 아니라 "필요한 것"만 받는다는 것 (나머지는 무시)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class KakaoPayReadyResponseVO {
	private String tid;//거래번호(Trade ID)
	//private String next_redirect_pc_url;//별도의 변환 설정이 없는 경우
	private String nextRedirectPcUrl;//카멜케이스 변환 설정이 있는 경우
}