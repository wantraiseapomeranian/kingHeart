package com.kh.shoppingmall.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.kh.shoppingmall.dto.MemberDto;
import com.kh.shoppingmall.dto.OrderDetailDto;
import com.kh.shoppingmall.mapper.OrderDetailMapper;

@Repository
public class OrderDetailDao {
	@Autowired
	private JdbcTemplate jdbcTemplate;
	@Autowired
	private OrderDetailMapper orderDetailMapper;
	
	public int sequence() {
		String sql = "select order_detail_seq.nextval from dual";
		return jdbcTemplate.queryForObject(sql, int.class);
	}
	
	//여러 주문건을 한번의 주문으로 통일
	//리턴 값이 1이면 정상이고 0 또는 -2면 삽입 실패
	public int[] batchInsert(List<OrderDetailDto> orderDetailList) {
		String sql = "insert into order_detail("
				+ "order_detail_no, order_no, "
				+ "product_no, option_no, "
				+ "order_amount, price_per_item"
				+ ") values(?, ?, ?, ?, ?, ?)";
		
		List<Object[]> paramsList = new ArrayList<>();
		
		for(OrderDetailDto dto : orderDetailList) {
			Object[] params = {
					dto.getOrderDetailNo(), dto.getOrderNo(), 
					dto.getProductNo(), dto.getOptionNo(), 
					dto.getOrderAmount(), dto.getPricePerItem()
			};
			paramsList.add(params);
		}
		
		
		return jdbcTemplate.batchUpdate(sql, paramsList);
	}
	
	public void insert(OrderDetailDto orderDetailDto) {
			String sql = "insert into order_detail("
			+ "order_detail_no, order_no, "
			+ "product_no, option_no, "
			+ "order_amount, price_per_item"
			+ ") values(?, ?, ?, ?, ?, ?)";

			Object[] params = {
				orderDetailDto.getOrderDetailNo(), orderDetailDto.getOrderNo(),
				orderDetailDto.getProductNo(), orderDetailDto.getOptionNo(),
				orderDetailDto.getOrderAmount(), orderDetailDto.getPricePerItem()
			};

			jdbcTemplate.update(sql, params);
	}
	
	public List<OrderDetailDto> selectListByOrdersNo(int ordersNo){
		String sql = "select * from order_detail where order_no = ?";
		Object[] param = {ordersNo};
		
		return jdbcTemplate.query(sql, orderDetailMapper, param);
	}
	
	public int deleteByOptionNoList(List<Integer> optionNoList) {
        if (optionNoList == null || optionNoList.isEmpty()) {
            return 0; // 삭제할 것이 없음
        }
        
        //IN 절에 들어갈 ? 생성
        String placeholders = String.join(",", Collections.nCopies(optionNoList.size(), "?"));

        //SQL 쿼리 생성
        String sql = "DELETE FROM order_detail WHERE option_no IN (" + placeholders + ")";
        
        //List<Integer>를 Object[] 배열로 변환
        Object[] params = optionNoList.toArray();
        
        return jdbcTemplate.update(sql, params);
    }
	
	//주문 상세 조회
	public OrderDetailDto selectOne(int orderDetailNo) {
		String sql = "select * from order_detail where order_detail_no = ?";
		
		Object[] params = {orderDetailNo};
		
		List<OrderDetailDto> list = jdbcTemplate.query(sql, orderDetailMapper, params);
		
		return list.isEmpty() ? null : list.get(0);
	}
	
	//부분 취소 시 상태 수정
	public boolean updateStatus(int orderDetailNo, String status) {
	    String sql = "update order_detail set detail_status = ? where order_detail_no = ?";
	    Object[] params = { status, orderDetailNo };
	    
	    return jdbcTemplate.update(sql, params) > 0;
	}
	
}
