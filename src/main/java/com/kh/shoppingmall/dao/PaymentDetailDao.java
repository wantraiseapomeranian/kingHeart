package com.kh.shoppingmall.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kh.shoppingmall.dto.PaymentDetailDto;
import com.kh.shoppingmall.mapper.PaymentDetailMapper;

@Repository
public class PaymentDetailDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PaymentDetailMapper paymentDetailMapper;

    // 시퀀스 번호 생성
    public long sequence() {
        String sql = "select payment_detail_seq.nextval from dual";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    // 결제 상세 항목 등록
    public void insert(PaymentDetailDto paymentDetailDto) {
        String sql = "insert into payment_detail ("
                + "payment_detail_no, payment_detail_origin, "
                + "payment_detail_item_no, payment_detail_item_name, "
                + "payment_detail_item_price, payment_detail_qty"
                + ") values (?, ?, ?, ?, ?, ?)";
        Object[] params = {
            paymentDetailDto.getPaymentDetailNo(), paymentDetailDto.getPaymentDetailOrigin(),
            paymentDetailDto.getPaymentDetailItemNo(), paymentDetailDto.getPaymentDetailItemName(),
            paymentDetailDto.getPaymentDetailItemPrice(), paymentDetailDto.getPaymentDetailQty()
        };
        jdbcTemplate.update(sql, params);
    }

    // 특정 결제 번호에 속한 상세 항목 리스트 조회
    public List<PaymentDetailDto> selectListByOrigin(long paymentDetailOrigin) {
        String sql = "select * from payment_detail where payment_detail_origin = ? order by payment_detail_no asc";
        return jdbcTemplate.query(sql, paymentDetailMapper, paymentDetailOrigin);
    }

    // 상세 항목 단일 조회
    public PaymentDetailDto selectOne(long paymentDetailNo) {
        String sql = "select * from payment_detail where payment_detail_no = ?";
        List<PaymentDetailDto> list = jdbcTemplate.query(sql, paymentDetailMapper, paymentDetailNo);
        return list.isEmpty() ? null : list.get(0);
    }

    // 특정 주문의 모든 항목 취소
    public boolean cancelAll(long paymentDetailOrigin) {
        String sql = "update payment_detail set payment_detail_status = '취소' where payment_detail_origin = ?";
        return jdbcTemplate.update(sql, paymentDetailOrigin) > 0;
    }

    // 개별 항목 취소
    public boolean cancelUnit(long paymentDetailNo) {
        String sql = "update payment_detail set payment_detail_status = '취소' where payment_detail_no = ?";
        return jdbcTemplate.update(sql, paymentDetailNo) > 0;
    }
}
