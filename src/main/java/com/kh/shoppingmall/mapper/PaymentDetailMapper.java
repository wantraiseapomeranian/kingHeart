package com.kh.shoppingmall.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import com.kh.shoppingmall.dto.PaymentDetailDto;

@Component
public class PaymentDetailMapper implements RowMapper<PaymentDetailDto> {
    @Override
    public PaymentDetailDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        return PaymentDetailDto.builder()
                .paymentDetailNo(rs.getLong("payment_detail_no"))
                .paymentDetailOrigin(rs.getLong("payment_detail_origin"))
                .paymentDetailItemNo(rs.getLong("payment_detail_item_no"))
                .paymentDetailItemName(rs.getString("payment_detail_item_name"))
                .paymentDetailItemPrice(rs.getInt("payment_detail_item_price"))
                .paymentDetailQty(rs.getInt("payment_detail_qty"))
                .paymentDetailStatus(rs.getString("payment_detail_status"))
            .build();
    }
}