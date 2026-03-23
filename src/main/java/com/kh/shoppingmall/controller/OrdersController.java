package com.kh.shoppingmall.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kh.shoppingmall.dao.MemberDao;
import com.kh.shoppingmall.dto.MemberDto;
import com.kh.shoppingmall.dto.OrdersDto;
import com.kh.shoppingmall.service.CartService;
import com.kh.shoppingmall.service.KakaoPayService;
import com.kh.shoppingmall.domain.order.service.OrderService;
import com.kh.shoppingmall.service.WishlistService;
import com.kh.shoppingmall.vo.CartDetailVO;
import com.kh.shoppingmall.vo.OrderListVO;
import com.kh.shoppingmall.vo.OrdersSummaryVO;
import com.kh.shoppingmall.vo.WishlistDetailVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayApproveRequestVO;
import com.kh.shoppingmall.vo.kakaopay.KakaoPayReadyResponseVO;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@CrossOrigin
@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrdersController {

	private final MemberDao memberDao;
	private final WishlistService wishlistService;
	private final OrderService orderService;
	private final CartService cartService;
	private final KakaoPayService kakaoPayService;

	// 위시리스트 페이지
	@GetMapping("/wishlist")
	public String wishlist(HttpSession session, Model model) {
		String memberId = (String) session.getAttribute("loginId");

		// 로그인 확인
		if (memberId == null)
			return "redirect:/member/login";

		List<WishlistDetailVO> wishlist = wishlistService.getWishlistItems(memberId);
		model.addAttribute("wishlist", wishlist); // 조회 결과를 모델에 추가
		return "/WEB-INF/views/orders/wishlist.jsp";
	}

	// 장바구니 페이지
	@GetMapping("/cart")
	public String cart(HttpSession session, Model model) {
		String memberId = (String) session.getAttribute("loginId");
		if (memberId == null) return "redirect:/member/login";

		List<CartDetailVO> cartlist = cartService.getCartItems(memberId);
		model.addAttribute("cartlist", cartlist); 
		
		//도착 예정일 계산 로직
		String formattedDeliveryDate = orderService.calculateEstimatedDeliveryDate();
	    model.addAttribute("estimatedDeliveryDate", formattedDeliveryDate);
	    
		return "/WEB-INF/views/orders/cart.jsp";
	}

	// 주문 작성(get, post로 나눠서 작성) 및 결제 페이지
	@GetMapping("/payment")
	public String payment(HttpSession session, Model model) {
		String memberId = (String) session.getAttribute("loginId");

		//로그인 확인
		if (memberId == null)
			return "redirect:/member/login";

		//장바구니 정보 조회
		List<CartDetailVO> cartItems = cartService.getCartItems(memberId);
		model.addAttribute("cartItems", cartItems);

		//회원 정보 조회
		MemberDto memberDto = memberDao.selectOne(memberId);
		model.addAttribute("memberDto", memberDto);

		//총 주문 금액 계산
		int totalPrice = orderService.calculateTotalPrice(cartItems);
		model.addAttribute("totalPrice", totalPrice);

		return "/WEB-INF/views/orders/payment.jsp";
	}

	@PostMapping("/payment")
	public String payment(HttpSession session, @ModelAttribute OrdersDto ordersDto) {
		String memberId = (String) session.getAttribute("loginId");

		List<CartDetailVO> cartItems = cartService.getCartItems(memberId);
		if (cartItems.isEmpty()) { 
			return "redirect:/orders/cart?error=empty";
		}
		
		//고유 주문번호 생성
		String partnerOrderId = java.util.UUID.randomUUID().toString();

		//ResponseVO 받아오기
		KakaoPayReadyResponseVO response = kakaoPayService.readyForCartItems(partnerOrderId, memberId, cartItems);

		//승인 단계에서 사용할 정보들을 세션에 임시 보관
		session.setAttribute("approve", KakaoPayApproveRequestVO.builder()
				.tid(response.getTid())
				.partnerOrderId(partnerOrderId)
				.partnerUserId(memberId)
			.build());
				
		session.setAttribute("ordersDto", ordersDto);

		return "redirect:" + response.getNextRedirectPcUrl();
	}
	
	@GetMapping("/payment/success/{partnerOrderId}")
	public String paymentSuccess(
			@PathVariable String partnerOrderId, @RequestParam String pg_token, HttpSession session) {
		
		KakaoPayApproveRequestVO approveRequest = (KakaoPayApproveRequestVO) session.getAttribute("approve");
		OrdersDto ordersDto = (OrdersDto) session.getAttribute("ordersDto");
		String memberId = (String) session.getAttribute("loginId");
		
		if (approveRequest == null || ordersDto == null) {
			return "redirect:/orders/cart?error=session_expired";
		}

		approveRequest.setPgToken(pg_token);
		List<CartDetailVO> cartItems = cartService.getCartItems(memberId);

		//주문번호 가져오기
		int ordersNo = orderService.createOrders(approveRequest, ordersDto, cartItems, session);

		session.removeAttribute("approve");
		session.removeAttribute("ordersDto");

		return "redirect:/orders/paymentcomplete?ordersNo=" + ordersNo;
	}

	//결제 취소
	@GetMapping("/payment/cancel")
	public String paymentCancel(HttpSession session) {
		session.removeAttribute("approve");
		session.removeAttribute("ordersDto");
		return "/WEB-INF/views/orders/payCancel.jsp";
	}
	
	//결제 실패
	@GetMapping("/payment/fail")
	public String paymentFail(HttpSession session) {
		session.removeAttribute("approve");
		session.removeAttribute("ordersDto");
		return "/WEB-INF/views/orders/payFail.jsp";
	}

	// 결제 완료 페이지
	@GetMapping("/paymentcomplete")
	public String paymentComplete(@RequestParam int ordersNo, Model model, HttpSession session) { // ordersNo 이름 일치
		String memberId = (String) session.getAttribute("loginId");
		if (memberId == null)
			return "redirect:/member/login";

		// 주문 정보 조회
		List<OrdersSummaryVO> orderSummaryList = orderService.getOrderSummary(ordersNo); // 변수명 변경 (List임을 명시)

		// 본인 주문 확인 + 주문 존재 확인
		if (orderSummaryList == null || orderSummaryList.isEmpty()
				|| !orderSummaryList.get(0).getOrdersId().equals(memberId)) {
			// 오류 처리 또는 리다이렉트
			return "redirect:/orders/list?error=notfound";
		}

		// 모델에 리스트 전체를 담아줌
		model.addAttribute("orderSummaryList", orderSummaryList); // 키 이름 변경

		return "/WEB-INF/views/orders/paymentcomplete.jsp"; // 슬래시(/) 시작 권장
	}

	// 주문 내역 확인 페이지
	@GetMapping("/list")
	public String list(HttpSession session, Model model) {
		String memberId = (String) session.getAttribute("loginId");

		// 로그인 확인
		if (memberId == null)
			return "redirect:/member/login";

		// 주문 내역 목록 조회
		List<OrderListVO> orderList = orderService.getOrderListSummaryByMember(memberId); // 예시 메소드명, Dto 또는 VO 사용
		model.addAttribute("orderList", orderList);

		return "/WEB-INF/views/orders/list.jsp";
	}

	// 주문 상세 내역 페이지
	@GetMapping("/detail")
	public String detail(@RequestParam int ordersNo, HttpSession session, Model model) {
		String memberId = (String) session.getAttribute("loginId");

		// 로그인 확인
		if (memberId == null)
			return "redirect:/member/login";
		
		List<OrdersSummaryVO> orderSummaryList = orderService.getOrderSummary(ordersNo);
		if (orderSummaryList == null || orderSummaryList.isEmpty()
				|| !orderSummaryList.get(0).getOrdersId().equals(memberId)) {
			// 오류 처리 또는 리다이렉트
			return "redirect:/orders/list?error=notfound";
		}
		
		model.addAttribute("orderSummaryList", orderSummaryList);

		return "/WEB-INF/views/orders/detail.jsp";
	}
	
	//주문 전체 취소 메소드
	@PostMapping("/cancel")
	public String cancel(@RequestParam int ordersNo, 
				HttpSession session, RedirectAttributes redirectAttributes
			) {
		String memberId = (String) session.getAttribute("loginId");
	    if (memberId == null) {
	        return "redirect:/member/login";
	    }

	    try {
	        boolean success = orderService.cancelOrder(ordersNo, memberId); // Service 호출
	        if (success) {
	            redirectAttributes.addFlashAttribute("message", "주문이 정상적으로 취소되었습니다.");
	        } else {
	            redirectAttributes.addFlashAttribute("error", "주문을 취소할 수 없습니다."); // 예: 이미 배송중
	        }
	    } catch (Exception e) {
	        // log.error("주문 취소 오류", e);
	        redirectAttributes.addFlashAttribute("error", "주문 취소 중 오류가 발생했습니다.");
	    }

	    return "redirect:/orders/list"; // 주문 내역 페이지로 리다이렉트
	}
	
	//부분 취소
	@PostMapping("/cancel/item")
	public String cancelItem(
	    @RequestParam int orderDetailNo,
	    @RequestParam int ordersNo,
	    HttpSession session, 
	    RedirectAttributes redirectAttributes
	) {
	    String memberId = (String) session.getAttribute("loginId");
	    if (memberId == null) {
	        return "redirect:/member/login";
	    }

	    try {
	        // 서비스의 부분 취소 로직 호출
	        boolean success = orderService.cancelOrderDetail(orderDetailNo, memberId);
	        
	        if (success) {
	            redirectAttributes.addFlashAttribute("message", "선택하신 상품이 취소되었습니다.");
	        } else {
	            redirectAttributes.addFlashAttribute("error", "상품 취소에 실패했습니다.");
	        }
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("error", "취소 처리 중 오류가 발생했습니다.");
	    }

	    //상세 페이지로 리다이렉트
	    return "redirect:/orders/detail?ordersNo=" + ordersNo;
	}
}
