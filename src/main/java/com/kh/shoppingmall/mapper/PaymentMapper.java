package com.kh.shoppingmall.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.kh.shoppingmall.dto.PaymentDto;

@Component
public class PaymentMapper implements RowMapper<PaymentDto> {
    @Override
    public PaymentDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PaymentDto.builder()
                .paymentNo(rs.getLong("payment_no"))
                .paymentOwner(rs.getString("payment_owner"))
                .paymentTid(rs.getString("payment_tid"))
                .paymentName(rs.getString("payment_name"))
                .paymentTotal(rs.getInt("payment_total"))
                .paymentRemain(rs.getInt("payment_remain"))
                .paymentTime(rs.getTimestamp("payment_time").toLocalDateTime())
            .build();
    }
}
