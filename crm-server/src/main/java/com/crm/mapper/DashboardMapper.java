package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Follow;
import com.crm.entity.Lead;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface DashboardMapper {

    @Select("""
            <script>
            select count(*) from crm_lead
             where deleted = 0 and owner_id is not null
               and create_time &gt;= date_format(curdate(), '%Y-%m-01')
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
            </script>
            """)
    long countLeadNew(@Param("ds") DataScope ds);

    @Select("""
            <script>
            select count(*) from crm_customer
             where deleted = 0
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
            </script>
            """)
    long countCustomer(@Param("ds") DataScope ds);

    /** 进行中的商机总额 */
    @Select("""
            <script>
            select ifnull(sum(amount), 0) from crm_opportunity
             where deleted = 0 and stage not in ('赢单', '输单')
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
            </script>
            """)
    BigDecimal sumOppAmount(@Param("ds") DataScope ds);

    /** 已回款金额 */
    @Select("""
            <script>
            select ifnull(sum(amount), 0) from crm_order
             where deleted = 0 and status in ('已付款', '已完成')
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
            </script>
            """)
    BigDecimal sumOrderPaid(@Param("ds") DataScope ds);

    @Select("""
            <script>
            select stage, count(*) as cnt, ifnull(sum(amount), 0) as amt
              from crm_opportunity where deleted = 0
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             group by stage
            </script>
            """)
    List<Map<String, Object>> funnel(@Param("ds") DataScope ds);

    @Select("""
            <script>
            select source, count(*) as cnt
              from crm_lead where deleted = 0 and source is not null
             <if test="ds != null and ds.scope == 4"> and owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             group by source order by cnt desc
            </script>
            """)
    List<Map<String, Object>> sources(@Param("ds") DataScope ds);

    /** 三天内要跟进或已逾期的 */
    @Select("""
            <script>
            select l.id, l.lead_no, l.lead_name, l.company, l.next_time, l.status, e.emp_name as owner_name
              from crm_lead l
              left join sys_employee e on e.id = l.owner_id
             where l.deleted = 0 and l.owner_id is not null and l.status in (0, 1)
               and l.next_time is not null
               and l.next_time &lt;= date_add(now(), interval 3 day)
             <if test="ds != null and ds.scope == 4"> and l.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and l.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by l.next_time asc limit 6
            </script>
            """)
    List<Lead> todo(@Param("ds") DataScope ds);

    @Select("""
            <script>
            select f.id, f.biz_type, f.biz_id, f.content, f.next_time, f.create_time, e.emp_name as create_by_name
              from crm_follow f
              left join sys_employee e on e.id = f.create_by
             where 1 = 1
             <if test="ds != null and ds.scope == 4"> and f.create_by = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and e.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by f.id desc limit 6
            </script>
            """)
    List<Follow> recent(@Param("ds") DataScope ds);
}
