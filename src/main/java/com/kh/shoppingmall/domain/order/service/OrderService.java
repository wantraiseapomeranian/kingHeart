package com.kh.shoppingmall.domain.order.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import com.kh.shoppingmall.dao.CartDao;
import com.kh.shoppingmall.dao.OrderListDao;
import com.kh.shoppingmall.dao.OrdersDao;
import com.kh.shoppingmall.dao.ProductDao;
import com.kh.shoppingmall.dao.ProductOptionDao;
import com.kh.shoppingmall.domain.order.entity.OrderDetail;
import com.kh.shoppingmall.domain.order.entity.Orders;
import com.kh.shoppingmall.domain.order.repository.OrderDetailRepository;
import com.kh.shoppingmall.domain.order.repository.OrdersRepository;
import com.kh.shoppingmall.dto.MemberDto;
import com.kh.shoppingmall.dto.OrdersDto;
import com.kh.shoppingmall.dto.ProductDto;
import com.kh.shoppingmall.service.KakaoPayService;
import com.kh.shoppingmall.service.MemberService;
import com.kh.shoppingmall.vo.CartDetailVO;
import com.kh.shoppingmall.vo.OrderListVO;
import com.kh.shoppingmall.vo.OrdersSummaryVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveResponseVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelResponseVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
	
	//새로 생성한 레포지토리
	private final OrdersRepository ordersRepository;
	private final OrderDetailRepository orderDetailRepository;
	
	//기존에 사용하던 클래스
	private final KakaoPayService kakaoPayService;
    private final ProductDao productDao;
    private final ProductOptionDao productOptionDao;
    private final CartDao cartDao;
    private final MemberService memberService;
    private final OrderListDao orderListDao;
    private final OrdersDao ordersDao;
    
    //총 주문 금액 계산
 	public int calculateTotalPrice(List<CartDetailVO> cartItems) {
 		int totalPrice = 0;

 		for (CartDetailVO cartItem : cartItems) {
 			ProductDto product = productDao.selectOne(cartItem.getProductNo());

 			if (product != null) {
 				int itemPrice = product.getProductPrice() * cartItem.getCartAmount();

 				totalPrice += itemPrice;
 			} else {
 				throw new RuntimeException("상품 정보를 찾을 수 없습니다: " + cartItem.getProductNo());
 			}
 		}

 		// 최종 금액 반환
 		return totalPrice;
 	}
    
    //주문 생성
    public int createOrders(KakaoPayApproveRequestVO approveRequest, OrdersDto ordersDto,List<CartDetailVO> cartItems, HttpSession session) {
    	
    	//카카오페이 승인 요청
    	KakaoPayApproveResponseVO responseVO = null;
    	
    	try {
            responseVO = kakaoPayService.approve(approveRequest);
            
        } catch (WebClientResponseException e) {
            log.error("🚨 카카오페이 승인 에러 발생 🚨");
            log.error("에러 상태 코드: {}", e.getStatusCode());
            log.error("에러 상세 내용: {}", e.getResponseBodyAsString()); 
            throw e;
        }
    	
    	//주문자 아이디 가져오기
    	String ordersId = (String) session.getAttribute("loginId");
    	
    	//엔티티 생성
    	Orders newOrder = Orders.builder()
    			.ordersId(ordersId)
    			.ordersTotalPrice(responseVO.getAmount().getTotal())
    			.ordersRecipient(ordersDto.getOrdersRecipient())
    			.ordersRecipientContact(ordersDto.getOrdersRecipientContact())
    			.ordersShippingPost(ordersDto.getOrdersShippingPost())
    			.ordersShippingAddress1(ordersDto.getOrdersShippingAddress1())
    			.ordersShippingAddress2(ordersDto.getOrdersShippingAddress2())
    			.ordersStatus("결제완료")
    			.ordersTid(responseVO.getTid())
    			.ordersItemName(responseVO.getItemName())
    			.ordersRemainPrice(responseVO.getAmount().getTotal())
    		.build();
    	
    	//장바구니 조회 후 OrderDetail 생성
    	for(CartDetailVO cartItem : cartItems) {
    		
    		//상품 조회 및 검증
    		ProductDto product = productDao.selectOne(cartItem.getProductNo());
    		if(product == null) 
    			throw new RuntimeException("상품 가격 정보를 찾을 수 없습니다: " + cartItem.getProductNo());
    		
    		//OrderDetail 생성
    		OrderDetail detail = OrderDetail.builder()
    				.productNo(cartItem.getProductNo())
                    .optionNo(cartItem.getOptionNo())
                    .orderAmount(cartItem.getCartAmount())
                    .pricePerItem(cartItem.getProductPrice())
                    .detailStatus("결제완료")
                   .build();
    		
    		//리스트에 추가
    		newOrder.addOrderDetail(detail);
    		
    		//재고 차감
    		boolean stockUpdated = productOptionDao.updateStock(cartItem.getOptionNo(), -cartItem.getCartAmount());
    		if (!stockUpdated)
    			throw new RuntimeException("재고 차감 실패: 재고가 부족합니다 (optionNo: " + cartItem.getOptionNo() + ")");
    	}
    	
    	//batchInsert 대체 메소드
    	Orders savedOrder = ordersRepository.save(newOrder);
    	
    	//장바구니 비우기
    	cartDao.deleteByMemberId(ordersId);
    	
    	//배송지 업데이트
    	if (ordersDto.isSaveAddressAsDefault()) { //체크박스가 체크되었다면
	        try {
	            // MemberDto 업데이트
	            MemberDto memberDto = new MemberDto();
	            memberDto.setMemberId(ordersId); //업데이트 대상 ID 설정
	          
	            memberDto.setMemberName(ordersDto.getOrdersRecipient());
	            memberDto.setMemberContact(ordersDto.getOrdersRecipientContact());
	            memberDto.setMemberPost(ordersDto.getOrdersShippingPost());
	            memberDto.setMemberAddress1(ordersDto.getOrdersShippingAddress1());
	            memberDto.setMemberAddress2(ordersDto.getOrdersShippingAddress2());

	            memberService.updateMemberAddress(memberDto);
	            
	            System.out.println("기본 배송지 정보 업데이트 완료: " + ordersId);

	        } catch (Exception e) {
	            System.err.println("기본 배송지 업데이트 중 오류 발생: " + ordersId + ", Error: " + e.getMessage());
	        }
	    }
    		
    	//주문번호 반환
    	return savedOrder.getOrdersNo();
    }
	
    //전체 취소
    @Transactional
    public boolean cancelOrder(int ordersNo, String memberId) {
        log.info("주문 취소 시작: {}", ordersNo);
        
        //주문 정보 확인
        Orders order = ordersRepository.findById(ordersNo)
                .orElse(null);

        if (order == null || !order.getOrdersId().equals(memberId)) {
            return false;
        }

        //취소 가능한 상태인지 확인 ('결제완료' 또는 '배송준비중')
        if (!order.getOrdersStatus().equals("결제완료") && !order.getOrdersStatus().equals("배송준비중")) {
            return false; // 취소 불가 상태
        }
        
        //카카오페이 결제 취소 API 호출
        KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
                .tid(order.getOrdersTid())
                .cancelAmount(order.getOrdersTotalPrice()) 
                .build();
        
        try {
            KakaoPayCancelResponseVO responseVO = kakaoPayService.cancel(requestVO);
            if (responseVO == null) {
                throw new RuntimeException("카카오페이 결제 취소 API 응답 실패");
            }
            
            //재고 복구 및 상세내역 상태 변경
            for (OrderDetail detail : order.getOrderDetails()) {
                boolean stockUpdated = productOptionDao.updateStock(detail.getOptionNo(), detail.getOrderAmount()); 
                if (!stockUpdated) {
                    throw new RuntimeException("재고 복구 중 오류 발생: 옵션 " + detail.getOptionNo());
                }
                detail.updateStatus("취소완료");
            }

            //부모 주문 상태 변경
            order.updateStatus("주문취소");
            
            return true;

        } catch (Exception e) {
            log.error("주문 취소 실패: {}", e.getMessage());
            throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다.", e);
        }
    }
    
    //부분 취소
    @Transactional
    public boolean cancelOrderDetail(int detailNo, String memberId) {
        
        //상세 정보 조회
        OrderDetail detail = orderDetailRepository.findById(detailNo)
                .orElseThrow(() -> new RuntimeException("상세 주문 내역이 없습니다."));
                
        //부모 주문 정보 가져오기 
        Orders order = detail.getOrders();

        //카카오페이 부분 취소 API 호출
        KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
                .tid(order.getOrdersTid())
                .cancelAmount(detail.getPricePerItem() * detail.getOrderAmount()) 
                .build();
        
        KakaoPayCancelResponseVO response = kakaoPayService.cancel(requestVO);

        if (response != null) {
            //해당 상세 항목 상태 변경
            detail.updateStatus("취소완료");
            
            //메인 주문 테이블의 잔액 차감
            order.updateRemainPrice(response.getCancelAvailableAmount().getTotal());
            
            //재고 복구
            productOptionDao.updateStock(detail.getOptionNo(), detail.getOrderAmount());
            
            //전체 취소 여부 확인
            boolean allCancelled = order.getOrderDetails().stream()
                    .allMatch(d -> "취소완료".equals(d.getDetailStatus()));
            
            if (allCancelled) {
                order.updateStatus("주문취소");
                log.info("주문 번호 {}의 모든 상품이 취소되어 전체 주문을 취소 처리했습니다.", order.getOrdersNo());
            }
            
            return true;
        }
        return false;
    }
    
    //배송일 계산 로직
  	public String calculateEstimatedDeliveryDate() {
  		java.time.LocalDate today = java.time.LocalDate.now(); 
          java.time.LocalDate estimatedDate = today.plusDays(4); 
          java.time.DayOfWeek dayOfWeek = estimatedDate.getDayOfWeek(); 

          if (dayOfWeek == java.time.DayOfWeek.SATURDAY) { 
              estimatedDate = estimatedDate.plusDays(2);
          } else if (dayOfWeek == java.time.DayOfWeek.SUNDAY) { 
              estimatedDate = estimatedDate.plusDays(1);
          }

          java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MM월 dd일(E)", java.util.Locale.KOREAN);
          return estimatedDate.format(formatter);
  	}
  	
    //memberId로 주문 내역 가져오기
  	public List<OrderListVO> getOrderListSummaryByMember(String memberId) {
  	    return orderListDao.selectList(memberId);
  	}
  	
  	//주문 목록 가져오기
  	public List<OrdersSummaryVO> getOrderSummary(int ordersNo) {
  	    List<OrdersSummaryVO> summaryList = ordersDao.selectOrderSummary(ordersNo);
  	    //주문 정보 자체가 없다면 null 반환
  	    if (summaryList.isEmpty()) {
  	        return null;
  	    }
  	    return summaryList;
  	}
}





