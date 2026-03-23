package com.kh.shoppingmall.domain.order.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "order_detail")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_detail_seq_gen")
    @SequenceGenerator(name = "order_detail_seq_gen", sequenceName = "order_detail_seq", allocationSize = 1)
    @Column(name = "order_detail_no")
    private Integer orderDetailNo;

    //N:1 양방향 매핑
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_no")
    private Orders orders;

    //외래키
    @Column(name = "product_no")
    private Integer productNo;
    
    //상품 옵션 번호
    @Column(name = "option_no")
    private Integer optionNo;
    
    //상품 주문 개수
    @Column(name = "order_amount")
    private int orderAmount;
    
    //상품 가격
    @Column(name = "price_per_item")
    private int pricePerItem;
    
    //주문 상태
    @Column(name = "detail_status")
    private String detailStatus;

    @Builder
    public OrderDetail(Integer productNo, Integer optionNo, int orderAmount, int pricePerItem, String detailStatus) {
        this.productNo = productNo;
        this.optionNo = optionNo;
        this.orderAmount = orderAmount;
        this.pricePerItem = pricePerItem;
        this.detailStatus = detailStatus;
    }

    // Orders 측에서 호출할 연관관계 세팅용 메서드
    protected void setOrders(Orders orders) {
        this.orders = orders;
    }
    
    //상세 상태 업데이트
    public void updateStatus(String status) {
        this.detailStatus = status;
    }
}