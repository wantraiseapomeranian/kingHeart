package com.kh.shoppingmall.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class KakaoPayConfiguration {
	@Autowired
	private KakaoPayProperties kakaoPayProperties;
	
	//카카오페이로 요청을 보내기 위한 도구 등록
	@Bean(name = "kakaopayWebClient")
	public WebClient webClient() {
		return WebClient.builder()
				.baseUrl("https://open-api.kakaopay.com")//시작주소 지정
				.defaultHeader("Authorization", "SECRET_KEY " + kakaoPayProperties.getSecretKey())//인증토큰
				.defaultHeader("Content-Type", "application/json")//전송데이터 유형설정
			.build();
	}
}