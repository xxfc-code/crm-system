package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Ticket;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface TicketMapper {

    @Select("""
            <script>
            select t.*, c.customer_name, e.emp_name as owner_name
              from crm_ticket t
              left join crm_customer c on c.id = t.customer_id
              left join sys_employee e on e.id = t.owner_id
             where t.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (t.title like concat('%', #{keyword}, '%') or t.ticket_no like concat('%', #{keyword}, '%')
                    or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null and status != ''"> and t.status = #{status} </if>
             <if test="ds != null and ds.scope == 4"> and t.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and t.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by t.id desc
            </script>
            """)
    List<Ticket> selectList(@Param("keyword") String keyword,
                            @Param("status") String status,
                            @Param("ds") DataScope ds);

    @Select("select * from crm_ticket where id = #{id} and deleted = 0")
    Ticket selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_ticket (customer_id, order_id, product_id, title, ticket_type, priority, status,
                                    content, owner_id, dept_id)
            values (#{customerId}, #{orderId}, #{productId}, #{title}, ifnull(#{ticketType}, '咨询'),
                    ifnull(#{priority}, '中'), ifnull(#{status}, '待受理'), #{content}, #{ownerId}, #{deptId})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Ticket ticket);

    @Update("update crm_ticket set ticket_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_ticket
               set customer_id = #{customerId}, order_id = #{orderId}, product_id = #{productId},
                   title = #{title}, ticket_type = #{ticketType},
                   priority = #{priority}, status = #{status}, content = #{content}, solution = #{solution},
                   owner_id = #{ownerId}, close_time = #{closeTime}
             where id = #{id}
            """)
    int update(Ticket ticket);

    @Update("""
            update crm_ticket
               set status = #{status},
                   close_time = if(#{status} in ('已解决', '已关闭'), now(), close_time)
             where id = #{id}
            """)
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    @Update("update crm_ticket set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_ticket where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
