package com.kh.shoppingmall.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kh.shoppingmall.dao.CartDao;
import com.kh.shoppingmall.dao.OrderDetailDao;
import com.kh.shoppingmall.dao.OrderListDao;
import com.kh.shoppingmall.dao.OrdersDao;
import com.kh.shoppingmall.dao.ProductDao;
import com.kh.shoppingmall.dao.ProductOptionDao;
import com.kh.shoppingmall.dto.MemberDto;
import com.kh.shoppingmall.dto.OrderDetailDto;
import com.kh.shoppingmall.dto.OrdersDto;
import com.kh.shoppingmall.dto.ProductDto;
import com.kh.shoppingmall.vo.CartDetailVO;
import com.kh.shoppingmall.vo.OrderListVO;
import com.kh.shoppingmall.vo.OrdersSummaryVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveResponseVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayCancelResponseVO;

import jakarta.servlet.http.HttpSession;

@Service
public class OrdersService {

	@Autowired
	private OrderDetailDao orderDetailDao;

	@Autowired
	private OrdersDao ordersDao;

	@Autowired
	private ProductOptionDao productOptionDao;

	@Autowired
	private CartDao cartDao;
	
	@Autowired
	private OrderListDao orderListDao;
	
	@Autowired
	private MemberService memberService;

	@Autowired
	private ProductDao productDao;
	
	//결제 관련
    @Autowired
    private KakaoPayService kakaoPayService;

	// 총 주문 금액 계산
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

	@Transactional
	public int createOrders(KakaoPayApproveRequestVO approveRequest, OrdersDto ordersDto, List<CartDetailVO> cartItems, HttpSession session) {
		
		//카카오페이 승인 요청
		KakaoPayApproveResponseVO responseVO = kakaoPayService.approve(approveRequest);
		
		//추가 정보 넣기
		ordersDto.setOrdersTid(responseVO.getTid());
	    ordersDto.setOrdersItemName(responseVO.getItemName());
	    ordersDto.setOrdersTotalPrice(responseVO.getAmount().getTotal());
	    ordersDto.setOrdersRemainPrice(responseVO.getAmount().getTotal());
		
		// 주문하는 사람 조회
		String ordersId = (String) session.getAttribute("loginId");
		
		//주문자 아이디 설정
		ordersDto.setOrdersId(ordersId);

		// 주문번호 받아오기
		int ordersNo = ordersDao.sequence();
		ordersDto.setOrdersNo(ordersNo);

		// 주문 상태 설정
		ordersDto.setOrdersStatus("결제완료");

		// 총 금액 계산 로직
//		int totalPrice = calculateTotalPrice(cartItems); // 별도 메소드로 계산

		// orders 테이블에 insert
		ordersDao.insert(ordersDto);

		//상세 내역 처리 및 재고 차감
		List<OrderDetailDto> orderDetailList = new ArrayList<>();
		
		for (CartDetailVO cartItem : cartItems) {
			
			//상품 정보 조회
			ProductDto product = productDao.selectOne(cartItem.getProductNo());
			
			//상품 정보가 없다면
			if (product == null)
				throw new RuntimeException("상품 가격 정보를 찾을 수 없습니다: " + cartItem.getProductNo());

			//orderDetailDto 생성
			OrderDetailDto orderDetailDto = OrderDetailDto.builder()
					//생성된 시퀀스로 order_detail_no insert
	                .orderDetailNo(orderDetailDao.sequence())
	                //주문번호 설정
	                .orderNo(ordersNo)
	                //CartDto의 정보를 OrderDetailDto로 복사
	                .productNo(cartItem.getProductNo())
	                .optionNo(cartItem.getOptionNo())
	                .orderAmount(cartItem.getCartAmount())
	                .pricePerItem(cartItem.getProductPrice())
	                .build();
			
			//리스트에 추가
	        orderDetailList.add(orderDetailDto);
	        
			//재고 차감
			boolean stockUpdated = productOptionDao.updateStock(cartItem.getOptionNo(), -cartItem.getCartAmount());
			
			if(!stockUpdated)
				throw new RuntimeException("재고 차감 실패: 재고가 부족합니다 (optionNo: " + cartItem.getOptionNo() + ")");
		}

		// DAO의 batchInsert 호출
		orderDetailDao.batchInsert(orderDetailList);

		//장바구니 비우기
		cartDao.deleteByMemberId(ordersId); 
		
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

		//생성된 주문 번호 반환
		return ordersNo; 
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
	
	//memberId로 주문 내역 가져오기
	public List<OrdersDto> getOrderListByMember(String memberId) {
	    return ordersDao.selectListByMemberId(memberId);
	}
	
	//memberId로 주문 내역 가져오기
	public List<OrderListVO> getOrderListSummaryByMember(String memberId) {
	    return orderListDao.selectList(memberId);
	}
	
	//전체 주문 취소
	@Transactional
	public boolean cancelOrder(int ordersNo, String memberId) {
		System.out.println("주문 취소 시작: " + ordersNo);
		
		//주문 정보 확인
	    OrdersDto order = ordersDao.selectOneByOrderNo(ordersNo);
	    System.out.println("주문 정보 확인 : " + order);
	    if (order == null || !order.getOrdersId().equals(memberId)) {
	        return false; //주문 없거나 내 주문 아님
	    }

	    //취소 가능한 상태인지 확인 ('결제완료' 또는 '배송준비중')
	    if (!order.getOrdersStatus().equals("결제완료") && !order.getOrdersStatus().equals("배송준비중")) {
	        return false; // 취소 불가 상태
	    }
	    
	    KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
                .tid(order.getOrdersTid())// DB에 저장된 거래번호(TID)
                .cancelAmount(order.getOrdersTotalPrice()) // 결제 금액
                .build();
	    
	    try {
            // 작성하신 WebClient 기반의 cancel 호출
            KakaoPayCancelResponseVO responseVO = kakaoPayService.cancel(requestVO);

            //응답이 없는 경우
            if (responseVO == null) {
                throw new RuntimeException("카카오페이 결제 취소 API 응답 실패");
            }
            
            //재고 복구 로직
            List<OrderDetailDto> details = orderDetailDao.selectListByOrdersNo(ordersNo);
    	    for (OrderDetailDto detail : details) {
    	    	System.out.println("optionNo : " + detail.getOptionNo() +  "detail" + detail.getOrderAmount());
    	        boolean stockUpdated = productOptionDao.updateStock(detail.getOptionNo(), detail.getOrderAmount()); // 수량만큼 다시 더함
    	        System.out.println("stockUpdated" + stockUpdated);
    	        if (!stockUpdated) {
    	            // 재고 복구 실패 시 롤백
    	            throw new RuntimeException("재고 복구 중 오류 발생: 옵션 " + detail.getOptionNo());
    	        }
    	    }

            //주문 상태 변경
            return ordersDao.update(ordersNo, "주문취소");

        } catch (Exception e) {
            System.err.println("주문 취소 실패: " + e.getMessage());
            throw new RuntimeException("결제 취소 처리 중 오류가 발생했습니다.", e);
        }
	}
	
	//전체 취소 되어있는지 조회
	private void checkAndCloseOrder(int ordersNo) {
	    //해당 주문의 모든 상세 내역을 가져옵니다.
	    List<OrderDetailDto> details = orderDetailDao.selectListByOrdersNo(ordersNo);

	    //모든 항목이 '취소완료' 상태인지 확인합니다.
	    boolean allCancelled = true;
	    for (OrderDetailDto detail : details) {
	        // 단 하나라도 '결제완료'나 다른 상태가 있다면 전체 취소가 아닙니다.
	        if (!"취소완료".equals(detail.getDetailStatus())) {
	            allCancelled = false;
	            break;
	        }
	    }

	    //만약 모든 항목이 취소되었다면 메인 주문 상태를 변경합니다.
	    if (allCancelled) {
	        ordersDao.update(ordersNo, "주문취소");
	        System.out.println("주문 번호 " + ordersNo + "의 모든 상품이 취소되어 전체 주문을 취소 처리했습니다.");
	    }
	}
	
	//부분 취소
	@Transactional
	public boolean cancelOrderDetail(int detailNo, String memberId) {
		
	    //상세 정보 조회
	    OrderDetailDto detail = orderDetailDao.selectOne(detailNo);
	    OrdersDto order = ordersDao.selectOneByOrderNo(detail.getOrderNo());

	    //카카오페이 부분 취소 API 호출
	    KakaoPayCancelRequestVO requestVO = KakaoPayCancelRequestVO.builder()
	            .tid(order.getOrdersTid())
	            .cancelAmount(detail.getPricePerItem() * detail.getOrderAmount()) // 해당 상품만큼만
	            .build();
	    
	    KakaoPayCancelResponseVO response = kakaoPayService.cancel(requestVO);

	    if (response != null) {
	        //해당 상세 항목만 상태 변경
	        orderDetailDao.updateStatus(detailNo, "취소완료");
	        
	        //메인 주문 테이블의 잔액 차감
	        ordersDao.updateRemainPrice(order.getOrdersNo(), response.getCancelAvailableAmount().getTotal());
	        
	        //해당 상품의 재고만 복구
	        productOptionDao.updateStock(detail.getOptionNo(), detail.getOrderAmount());
	        
	        //만약 모든 상세 항목이 취소되었다면 전체 주문 상태도 '주문취소'로 변경
	        checkAndCloseOrder(order.getOrdersNo());
	        
	        return true;
	    }
	    return false;
	}
}
