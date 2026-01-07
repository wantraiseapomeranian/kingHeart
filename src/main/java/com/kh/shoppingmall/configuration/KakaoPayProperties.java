package com.kh.shoppingmall.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "custom.kakaopay")
public class KakaoPayProperties {
	private String cid;//custom.kakaopay.cid
	private String secretKey;//custom.kakaopay.secret-key
}