package com.kh.shoppingmall.domain.order.entity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orders {
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders_seq_gen")
	@SequenceGenerator(name = "orders_seq_gen", sequenceName = "orders_seq", allocationSize = 1)
	@Column(name = "orders_no")
    private Integer ordersNo;
	
	//member_id 외래키
	@Column(name = "orders_id")
    private String ordersId; 
	
	//총 주문 금액
    @Column(name = "orders_totalprice")
    private int ordersTotalPrice;
    
    //주문자 성명
    @Column(name = "orders_recipient")
    private String ordersRecipient;
    
    //주문자 전화번호
    @Column(name = "orders_recipientcontact")
    private String ordersRecipientContact;
    
    //주문자 우편번호
    @Column(name = "orders_shippingpost")
    private String ordersShippingPost;
    
    //주문자 기본주소
    @Column(name = "orders_shippingaddress1")
    private String ordersShippingAddress1;
    
    //주문자 상세주소
    @Column(name = "orders_shippingaddress2")
    private String ordersShippingAddress2;
    
    //주문 상태
    @Column(name = "orders_status")
    private String ordersStatus;
    
    //주문일
    @CreationTimestamp
	@Column(name = "orders_created_at", updatable = false)
    private Timestamp ordersCreatedAt;
    
    //주문 tid(카카오페이 전용)
    @Column(name = "orders_tid")
    private String ordersTid;
    
    //주문 상품명
    @Column(name = "orders_item_name")
    private String ordersItemName;
    
    //주문 남은 금액
    @Column(name = "orders_remain_price")
    private int ordersRemainPrice;
    
    //OrderDetail 테이블 1:N 설정
    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails = new ArrayList<>();
    
    @Builder
    public Orders(String ordersId, int ordersTotalPrice, String ordersRecipient, String ordersRecipientContact, String ordersShippingPost, String ordersShippingAddress1, String ordersShippingAddress2, String ordersStatus, String ordersTid, String ordersItemName, int ordersRemainPrice) {
        this.ordersId = ordersId;
        this.ordersTotalPrice = ordersTotalPrice;
        this.ordersRecipient = ordersRecipient;
        this.ordersRecipientContact = ordersRecipientContact;
        this.ordersShippingPost = ordersShippingPost;
        this.ordersShippingAddress1 = ordersShippingAddress1;
        this.ordersShippingAddress2 = ordersShippingAddress2;
        this.ordersStatus = ordersStatus;
        this.ordersTid = ordersTid;
        this.ordersItemName = ordersItemName;
        this.ordersRemainPrice = ordersRemainPrice;
    }
    
    //연관관계 편의 메서드
    public void addOrderDetail(OrderDetail orderDetail) {
        this.orderDetails.add(orderDetail);
        orderDetail.setOrders(this);
    }

    //부분 취소 시 남은 금액 업데이트
    public void updateRemainPrice(int remainPrice) {
        this.ordersRemainPrice = remainPrice;
    }

    public void updateStatus(String status) {
        this.ordersStatus = status;
        this.ordersRemainPrice = 0;
    }
    
}





