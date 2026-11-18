package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Payment;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface PaymentMapper {

    @Select("""
            <script>
            select p.*, c.customer_name, e.emp_name as owner_name, o.order_no
              from crm_payment p
              left join crm_customer c on c.id = p.customer_id
              left join sys_employee e on e.id = p.owner_id
              left join crm_order o on o.id = p.order_id
             where p.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (p.payment_no like concat('%', #{keyword}, '%') or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="payType != null and payType != ''"> and p.pay_type = #{payType} </if>
             <if test="ds != null and ds.scope == 4"> and p.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and p.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by p.id desc
            </script>
            """)
    List<Payment> selectList(@Param("keyword") String keyword,
                             @Param("payType") String payType,
                             @Param("ds") DataScope ds);

    @Select("select * from crm_payment where id = #{id} and deleted = 0")
    Payment selectById(@Param("id") Long id);

    /** 这张退换货单是否已经冲销过 */
    @Select("select count(*) from crm_payment where return_id = #{returnId} and deleted = 0")
    int countByReturn(@Param("returnId") Long returnId);

    /** 某张订单已收金额合计 */
    @Select("select ifnull(sum(amount), 0) from crm_payment where order_id = #{orderId} and deleted = 0")
    java.math.BigDecimal sumByOrder(@Param("orderId") Long orderId);

    /** 最近一次回款日期 */
    @Select("select max(pay_date) from crm_payment where order_id = #{orderId} and deleted = 0")
    java.time.LocalDate lastPayDate(@Param("orderId") Long orderId);

    @Insert("""
            insert into crm_payment (order_id, return_id, customer_id, amount, pay_date, pay_type, owner_id, dept_id, remark)
            values (#{orderId}, #{returnId}, #{customerId}, ifnull(#{amount}, 0), #{payDate}, #{payType},
                    #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Payment payment);

    @Update("update crm_payment set payment_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_payment
               set order_id = #{orderId}, customer_id = #{customerId}, amount = ifnull(#{amount}, 0),
                   pay_date = #{payDate}, pay_type = #{payType}, owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(Payment payment);

    @Update("update crm_payment set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_payment where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
