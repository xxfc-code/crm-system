package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Customer;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface CustomerMapper {

    @Select("""
            <script>
            select c.*, e.emp_name as owner_name, pe.emp_name as pre_owner_name,
                   (select count(*) from crm_opportunity o where o.customer_id = c.id and o.deleted = 0) as opp_count,
                   (select count(*) from crm_order od where od.customer_id = c.id and od.deleted = 0) as order_count
              from crm_customer c
              left join sys_employee e on e.id = c.owner_id
              left join sys_employee pe on pe.id = c.pre_owner_id
             where c.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (c.customer_name like concat('%', #{keyword}, '%')
                    or c.contact like concat('%', #{keyword}, '%')
                    or c.phone like concat('%', #{keyword}, '%'))
             </if>
             <if test="level != null and level != ''"> and c.level = #{level} </if>
             <if test="ds != null and ds.scope == 4"> and c.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and c.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by c.id desc
            </script>
            """)
    List<Customer> selectList(@Param("keyword") String keyword,
                              @Param("level") String level,
                              @Param("ds") DataScope ds);

    /** 客户公海：没有负责人的客户，所有人可见可领 */
    @Select("""
            <script>
            select c.*, pe.emp_name as pre_owner_name,
                   (select count(*) from crm_order od where od.customer_id = c.id and od.deleted = 0) as order_count
              from crm_customer c
              left join sys_employee pe on pe.id = c.pre_owner_id
             where c.deleted = 0 and c.owner_id is null
             <if test="keyword != null and keyword != ''">
               and (c.customer_name like concat('%', #{keyword}, '%') or c.contact like concat('%', #{keyword}, '%'))
             </if>
             order by c.pool_time desc, c.id desc
            </script>
            """)
    List<Customer> selectPool(@Param("keyword") String keyword);

    /** A 类重点客户，长期没跟进的排前面 */
    @Select("""
            <script>
            select c.id, c.customer_no, c.customer_name, c.contact, c.phone, c.level, c.last_follow_time
              from crm_customer c
             where c.deleted = 0 and c.level = 'A'
             <if test="ds != null and ds.scope == 4"> and c.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and c.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by c.last_follow_time asc
             limit 5
            </script>
            """)
    List<Customer> selectKeyCustomers(@Param("ds") DataScope ds);

    @Select("select * from crm_customer where id = #{id} and deleted = 0")
    Customer selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_customer (customer_name, store_no, sign_date, region, first_order_date, store_area,
                                      contact, phone, email, level, industry, address, source,
                                      owner_id, dept_id, lead_id, remark, last_follow_time)
            values (#{customerName}, #{storeNo}, #{signDate}, #{region}, #{firstOrderDate}, #{storeArea},
                    #{contact}, #{phone}, #{email}, ifnull(#{level}, 'C'), #{industry},
                    #{address}, #{source}, #{ownerId}, #{deptId}, #{leadId}, #{remark}, now())
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Customer customer);

    @Update("update crm_customer set customer_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_customer
               set customer_name = #{customerName}, store_no = #{storeNo}, sign_date = #{signDate},
                   region = #{region}, first_order_date = #{firstOrderDate}, store_area = #{storeArea},
                   contact = #{contact}, phone = #{phone},
                   email = #{email}, level = ifnull(#{level}, 'C'), industry = #{industry},
                   address = #{address}, source = #{source}, owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(Customer customer);

    /** 跟进了客户就刷新活跃时间 */
    @Update("update crm_customer set last_follow_time = now() where id = #{id}")
    int touchFollow(@Param("id") Long id);

    /** 从公海领取客户，只有仍无主时才能领 */
    @Update("""
            update crm_customer
               set owner_id = #{userId}, dept_id = #{deptId}, pool_time = null, pool_reason = null,
                   last_follow_time = now()
             where id = #{id} and deleted = 0 and owner_id is null
            """)
    int claim(@Param("id") Long id, @Param("userId") Long userId, @Param("deptId") Long deptId);

    /** 长期无跟进（或销售离职）释放客户；重点客户 A 不回收 */
    @Update("""
            update crm_customer
               set pre_owner_id = owner_id,
                   owner_id = null,
                   pool_time = now(),
                   pool_reason = '长期未跟进'
             where deleted = 0 and owner_id is not null and level != 'A'
               and last_follow_time < #{before}
            """)
    int recycleToPool(@Param("before") LocalDateTime before);

    @Update("update crm_customer set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_customer where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
