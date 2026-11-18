package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.CrmOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface OrderMapper {

    @Select("""
            <script>
            select o.*, c.customer_name, e.emp_name as owner_name, op.opp_name,
                   (select max(p.pay_date) from crm_payment p where p.order_id = o.id and p.deleted = 0) as last_pay_date,
                   (select ifnull(sum(p.amount), 0) from crm_payment p where p.order_id = o.id and p.deleted = 0) as paid_amount,
                   (select ifnull(sum(i.cost * i.qty), 0) from crm_order_item i where i.order_id = o.id) as cost_total
              from crm_order o
              left join crm_customer c on c.id = o.customer_id
              left join sys_employee e on e.id = o.owner_id
              left join crm_opportunity op on op.id = o.opp_id
             where o.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (o.order_no like concat('%', #{keyword}, '%')
                    or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null and status != ''"> and o.status = #{status} </if>
             <if test="ds != null and ds.scope == 4"> and o.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and o.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by o.id desc
            </script>
            """)
    List<CrmOrder> selectList(@Param("keyword") String keyword,
                              @Param("status") String status,
                              @Param("ds") DataScope ds);

    @Select("""
            select o.*, c.customer_name, e.emp_name as owner_name, op.opp_name,
                   (select max(p.pay_date) from crm_payment p where p.order_id = o.id and p.deleted = 0) as last_pay_date,
                   (select ifnull(sum(p.amount), 0) from crm_payment p where p.order_id = o.id and p.deleted = 0) as paid_amount,
                   (select ifnull(sum(i.cost * i.qty), 0) from crm_order_item i where i.order_id = o.id) as cost_total
              from crm_order o
              left join crm_customer c on c.id = o.customer_id
              left join sys_employee e on e.id = o.owner_id
              left join crm_opportunity op on op.id = o.opp_id
             where o.id = #{id} and o.deleted = 0
            """)
    CrmOrder selectById(@Param("id") Long id);

    /** 这张报价单是否已经生成过订单 */
    @Select("select count(*) from crm_order where quote_id = #{quoteId} and deleted = 0")
    int countByQuote(@Param("quoteId") Long quoteId);

    @Insert("""
            insert into crm_order (customer_id, opp_id, quote_id, amount, status, sign_date,
                                   owner_id, dept_id, remark)
            values (#{customerId}, #{oppId}, #{quoteId}, ifnull(#{amount}, 0), ifnull(#{status}, '待付款'),
                    #{signDate}, #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(CrmOrder order);

    @Update("update crm_order set order_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_order
               set customer_id = #{customerId}, opp_id = #{oppId}, quote_id = #{quoteId},
                   amount = ifnull(#{amount}, 0), status = ifnull(#{status}, '待付款'),
                   sign_date = #{signDate}, owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(CrmOrder order);

    @Update("update crm_order set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update crm_order set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_order where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
