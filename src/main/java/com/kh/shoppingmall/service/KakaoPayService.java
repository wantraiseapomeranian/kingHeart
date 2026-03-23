package com.kh.shoppingmall.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.kh.shoppingmall.configuration.KakaoPayProperties;
import com.kh.shoppingmall.vo.CartDetailVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveResponseVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelResponseVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayOrderRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayOrderResponseVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayReadyRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayReadyResponseVO;

@Service
public class KakaoPayService {
	@Autowired
	@Qualifier("kakaopayWebClient")
	private WebClient webClient;
	@Autowired
	private KakaoPayProperties kakaoPayProperties;
	
	//결제 준비
	public KakaoPayReadyResponseVO ready(KakaoPayReadyRequestVO requestVO) {
		//Body 준비 (카카오페이에서 요구하는 Request 데이터)
		Map<String, String> body = new HashMap<>();
		//body에 필요한 정보들을 담음
		body.put("cid", kakaoPayProperties.getCid());//가맹점코드
		body.put("partner_order_id", requestVO.getPartnerOrderId());//주문번호
		body.put("partner_user_id", requestVO.getPartnerUserId());//구매자ID
		body.put("item_name", requestVO.getItemName());//구매상품명
		body.put("quantity", "1");//수량(무조건 1, 우리가 관리)
		body.put("total_amount", String.valueOf(requestVO.getTotalAmount()));//판매금액
		body.put("tax_free_amount", "0");//비과세액(해당없음, 0으로 설정)
		
		//(+추가) 주소가 한 페이지로 고정되면 안된다 (모든 처리를 한곳에서 할 수는 없으므로)
		// - 현재 요청중인 페이지의 뒤에 /success , /cancel , /fail을 추가시켜서 설정
		String currentPath = ServletUriComponentsBuilder.fromCurrentRequest().toUriString();
		body.put("approval_url", currentPath + "/success/" + requestVO.getPartnerOrderId());//성공 시 연결할 주소(카카오페이에 등록된 플랫폼 주소)
		body.put("cancel_url", currentPath + "/cancel/" + requestVO.getPartnerOrderId());//취소 시 연결할 주소(카카오페이에 등록된 플랫폼 주소)
		body.put("fail_url", currentPath + "/fail/" + requestVO.getPartnerOrderId());//실패 시 연결할 주소(카카오페이에 등록된 플랫폼 주소)
		
		//WebClient에 정보를 모두 담아 주소를 알려주고 요청을 보낸 뒤 응답 수신
		//카카오페이 문서에 있는 결제 준비 응답 데이터(Response)
		KakaoPayReadyResponseVO response = webClient.post()//POST 요청
//						.uri("https://open-api.kakaopay.com/online/v1/payment/ready")//요청주소
				.uri("/online/v1/payment/ready")//webClient에 기본주소 설정이 있을 경우
				.bodyValue(body)//요청에 첨부할 데이터 설정
			.retrieve()//응답을 수신하겠다
				.bodyToMono(KakaoPayReadyResponseVO.class)//데이터는 한번에 오고(Mono) 형태는 Map이다 (↔ 연속적으로 오면 Flux)
				.block();//동기적으로 변환하여 응답이 올때까지 기다려라! (RestTemplate과 같아짐)
		
		return response;
	}
	//결제 승인
	public KakaoPayApproveResponseVO approve(KakaoPayApproveRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		//body에 필요한 정보들을 담음 (카카오페이 문서 확인)
		body.put("cid", kakaoPayProperties.getCid());
		body.put("partner_order_id", requestVO.getPartnerOrderId());
		body.put("partner_user_id", requestVO.getPartnerUserId());
		body.put("tid", requestVO.getTid());
		body.put("pg_token", requestVO.getPgToken());
		
		KakaoPayApproveResponseVO response = webClient.post()//POST 요청
				.uri("/online/v1/payment/approve")//webClient에 기본주소 설정이 있을 경우
				.bodyValue(body)//요청에 첨부할 데이터 설정
			.retrieve()//응답을 수신하겠다
				.bodyToMono(KakaoPayApproveResponseVO.class)//데이터는 한번에 오고(Mono) 형태는 KakaoPayApproveResponseVO이다 (↔ 연속적으로 오면 Flux)
				.block();//동기적으로 변환하여 응답이 올때까지 기다려라! (RestTemplate과 같아짐)
		
		return response;
	}
	
	//결제 조회
	public KakaoPayOrderResponseVO order(KakaoPayOrderRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		//데이터 추가
		body.put("cid", kakaoPayProperties.getCid());
		body.put("tid", requestVO.getTid());
		
		KakaoPayOrderResponseVO responseVO = webClient.post()//POST 요청
				.uri("/online/v1/payment/order")//webClient에 기본주소 설정이 있을 경우
				.bodyValue(body)//요청에 첨부할 데이터 설정
			.retrieve()//응답을 수신하겠다
				.bodyToMono(KakaoPayOrderResponseVO.class)//데이터는 한번에 오고(Mono) 형태는 KakaoPayApproveResponseVO이다 (↔ 연속적으로 오면 Flux)
				.block();//동기적으로 변환하여 응답이 올때까지 기다려라! (RestTemplate과 같아짐)
		
		return responseVO;
	}
	
	//결제 취소
	public KakaoPayCancelResponseVO cancel(KakaoPayCancelRequestVO requestVO) {
		Map<String, String> body = new HashMap<>();
		//body에 필요한 정보들을 담음 (카카오페이 문서 확인)
		body.put("cid", kakaoPayProperties.getCid());
		body.put("tid", requestVO.getTid());
		body.put("cancel_amount", String.valueOf(requestVO.getCancelAmount()));
		body.put("cancel_tax_free_amount", "0");
		
		KakaoPayCancelResponseVO response = webClient.post()//POST 요청
				.uri("/online/v1/payment/cancel")//webClient에 기본주소 설정이 있을 경우
				.bodyValue(body)//요청에 첨부할 데이터 설정
			.retrieve()//응답을 수신하겠다
				.bodyToMono(KakaoPayCancelResponseVO.class)//데이터는 한번에 오고(Mono) 형태는 KakaoPayApproveResponseVO이다 (↔ 연속적으로 오면 Flux)
				.block();//동기적으로 변환하여 응답이 올때까지 기다려라! (RestTemplate과 같아짐)
		
		return response;
	}
	
	public KakaoPayReadyResponseVO readyForCartItems(String partnerOrderId, String memberId, List<CartDetailVO> cartItems) {
        String itemName = cartItems.get(0).getProductName();
        if (cartItems.size() > 1) {
            itemName += " 외 " + (cartItems.size() - 1) + "건";
        }
        
        int totalPrice = cartItems.stream()
                .mapToInt(item -> item.getProductPrice() * item.getCartAmount())
                .sum();

        KakaoPayReadyRequestVO readyRequest = KakaoPayReadyRequestVO.builder()
                .partnerOrderId(partnerOrderId)
                .partnerUserId(memberId)
                .itemName(itemName)
                .totalAmount(totalPrice)
               .build();
        
        return this.ready(readyRequest);
	}
}





