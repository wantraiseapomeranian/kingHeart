package com.kh.shoppingmall.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kh.shoppingmall.dto.PaymentDto;
import com.kh.shoppingmall.mapper.PaymentMapper;

@Repository
public class PaymentDao {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PaymentMapper paymentMapper;

    // 시퀀스 번호 생성
    public long sequence() {
        String sql = "select payment_seq.nextval from dual";
        return jdbcTemplate.queryForObject(sql, Long.class);
    }

    // 결제 마스터 정보 등록
    public void insert(PaymentDto paymentDto) {
        String sql = "insert into payment ("
                + "payment_no, payment_owner, payment_tid, "
                + "payment_name, payment_total, payment_remain"
                + ") values (?, ?, ?, ?, ?, ?)";
        Object[] params = {
            paymentDto.getPaymentNo(), paymentDto.getPaymentOwner(),
            paymentDto.getPaymentTid(), paymentDto.getPaymentName(),
            paymentDto.getPaymentTotal(), paymentDto.getPaymentRemain()
        };
        jdbcTemplate.update(sql, params);
    }

    // 회원의 결제 목록 조회
    public List<PaymentDto> selectListByOwner(String loginId) {
        String sql = "select * from payment where payment_owner = ? order by payment_no desc";
        return jdbcTemplate.query(sql, paymentMapper, loginId);
    }

    // 결제 상세 단일 조회
    public PaymentDto selectOne(long paymentNo) {
        String sql = "select * from payment where payment_no = ?";
        List<PaymentDto> list = jdbcTemplate.query(sql, paymentMapper, paymentNo);
        return list.isEmpty() ? null : list.get(0);
    }

    // 전체 취소
    public boolean cancelAll(long paymentNo) {
        String sql = "update payment set payment_remain = 0 where payment_no = ?";
        return jdbcTemplate.update(sql, paymentNo) > 0;
    }

    // 부분 취소
    public boolean cancelUnit(long paymentNo, int paymentRemain) {
        String sql = "update payment set payment_remain = ? where payment_no = ?";
        return jdbcTemplate.update(sql, paymentRemain, paymentNo) > 0;
    }
}
