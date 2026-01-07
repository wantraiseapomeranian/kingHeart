package com.kh.shoppingmall.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PaymentDto {
	
	private Long paymentNo;
	private String paymentOwner;
	private String paymentTid;
	private String paymentName;
	private Integer paymentTotal;
	private Integer paymentRemain;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
	private LocalDateTime paymentTime;
}
