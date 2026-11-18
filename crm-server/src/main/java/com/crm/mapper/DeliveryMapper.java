package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Delivery;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DeliveryMapper {

    @Select("""
            <script>
            select d.*, c.customer_name, e.emp_name as owner_name, o.order_no
              from crm_delivery d
              left join crm_customer c on c.id = d.customer_id
              left join sys_employee e on e.id = d.owner_id
              left join crm_order o on o.id = d.order_id
             where d.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (d.delivery_no like concat('%', #{keyword}, '%') or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null and status != ''"> and d.status = #{status} </if>
             <if test="ds != null and ds.scope == 4"> and d.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and d.dept_id in
                   <foreach collection="ds.deptIds" item="dd" open="(" separator="," close=")">#{dd}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by d.id desc
            </script>
            """)
    List<Delivery> selectList(@Param("keyword") String keyword,
                              @Param("status") String status,
                              @Param("ds") DataScope ds);

    @Select("select * from crm_delivery where id = #{id} and deleted = 0")
    Delivery selectById(@Param("id") Long id);

    /** 该订单的发货记录数 */
    @Select("select count(*) from crm_delivery where order_id = #{orderId} and deleted = 0")
    int countByOrder(@Param("orderId") Long orderId);

    /** 已签收的发货记录数 */
    @Select("select count(*) from crm_delivery where order_id = #{orderId} and deleted = 0 and status = '已签收'")
    int countSigned(@Param("orderId") Long orderId);

    @Insert("""
            insert into crm_delivery (order_id, customer_id, delivery_date, receiver, phone, address,
                                      status, owner_id, dept_id, remark)
            values (#{orderId}, #{customerId}, #{deliveryDate}, #{receiver}, #{phone}, #{address},
                    ifnull(#{status}, '待发货'), #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Delivery delivery);

    @Update("update crm_delivery set delivery_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_delivery
               set order_id = #{orderId}, customer_id = #{customerId}, delivery_date = #{deliveryDate},
                   receiver = #{receiver}, phone = #{phone}, address = #{address},
                   status = ifnull(#{status}, '待发货'), owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(Delivery delivery);

    @Update("update crm_delivery set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update crm_delivery set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_delivery where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
