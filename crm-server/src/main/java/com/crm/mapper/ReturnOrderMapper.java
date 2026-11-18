package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.ReturnOrder;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ReturnOrderMapper {

    @Select("""
            <script>
            select r.*, c.customer_name, e.emp_name as owner_name, o.order_no
              from crm_return_order r
              left join crm_customer c on c.id = r.customer_id
              left join sys_employee e on e.id = r.owner_id
              left join crm_order o on o.id = r.order_id
             where r.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (r.return_no like concat('%', #{keyword}, '%') or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null and status != ''"> and r.status = #{status} </if>
             <if test="ds != null and ds.scope == 4"> and r.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and r.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by r.id desc
            </script>
            """)
    List<ReturnOrder> selectList(@Param("keyword") String keyword,
                                 @Param("status") String status,
                                 @Param("ds") DataScope ds);

    @Select("select * from crm_return_order where id = #{id} and deleted = 0")
    ReturnOrder selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_return_order (order_id, customer_id, return_type, reason, amount, status,
                                          handle_date, owner_id, dept_id, remark)
            values (#{orderId}, #{customerId}, ifnull(#{returnType}, '退货'), #{reason}, ifnull(#{amount}, 0),
                    ifnull(#{status}, '待处理'), #{handleDate}, #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ReturnOrder order);

    @Update("update crm_return_order set return_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_return_order
               set order_id = #{orderId}, customer_id = #{customerId}, return_type = #{returnType},
                   reason = #{reason}, amount = ifnull(#{amount}, 0), status = ifnull(#{status}, '待处理'),
                   handle_date = #{handleDate}, owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(ReturnOrder order);

    @Update("update crm_return_order set status = #{status}, handle_date = #{handleDate} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status,
                     @Param("handleDate") java.time.LocalDate handleDate);

    @Update("update crm_return_order set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_return_order where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
