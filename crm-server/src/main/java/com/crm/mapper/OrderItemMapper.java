package com.crm.mapper;

import com.crm.entity.OrderItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OrderItemMapper {

    @Select("select * from crm_order_item where order_id = #{orderId} order by id")
    List<OrderItem> selectByOrder(@Param("orderId") Long orderId);

    @Insert("""
            insert into crm_order_item (order_id, product_id, product_name, spec, unit, qty, price, cost, amount)
            values (#{orderId}, #{productId}, #{productName}, #{spec}, #{unit}, #{qty}, #{price}, #{cost}, #{amount})
            """)
    int insert(OrderItem item);

    @Delete("delete from crm_order_item where order_id = #{orderId}")
    int deleteByOrder(@Param("orderId") Long orderId);
}
