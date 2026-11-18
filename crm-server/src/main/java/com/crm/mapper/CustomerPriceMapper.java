package com.crm.mapper;

import com.crm.entity.CustomerPrice;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface CustomerPriceMapper {

    @Select("""
            <script>
            select cp.*, c.customer_name, p.product_name, p.price as standard_price
              from crm_customer_price cp
              left join crm_customer c on c.id = cp.customer_id
              left join crm_product p on p.id = cp.product_id
             where cp.deleted = 0
             <if test="customerId != null"> and cp.customer_id = #{customerId} </if>
             order by cp.id desc
            </script>
            """)
    List<CustomerPrice> selectList(@Param("customerId") Long customerId);

    @Select("select * from crm_customer_price where id = #{id} and deleted = 0")
    CustomerPrice selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_customer_price (customer_id, product_id, price, remark)
            values (#{customerId}, #{productId}, ifnull(#{price}, 0), #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CustomerPrice price);

    @Update("""
            update crm_customer_price
               set customer_id = #{customerId}, product_id = #{productId},
                   price = ifnull(#{price}, 0), remark = #{remark}
             where id = #{id}
            """)
    int update(CustomerPrice price);

    @Update("update crm_customer_price set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_customer_price where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
