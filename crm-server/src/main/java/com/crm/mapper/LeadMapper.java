package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Lead;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

public interface LeadMapper {

    /** 线索列表，只查有负责人的（公海单独查） */
    @Select("""
            <script>
            select l.*, e.emp_name as owner_name, d.dept_name, pe.emp_name as pre_owner_name
              from crm_lead l
              left join sys_employee e on e.id = l.owner_id
              left join sys_dept d on d.id = l.dept_id
              left join sys_employee pe on pe.id = l.pre_owner_id
             where l.deleted = 0 and l.owner_id is not null
             <if test="keyword != null and keyword != ''">
               and (l.lead_name like concat('%', #{keyword}, '%')
                    or l.company like concat('%', #{keyword}, '%')
                    or l.phone like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null"> and l.status = #{status} </if>
             <if test="source != null and source != ''"> and l.source = #{source} </if>
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
             order by l.id desc
            </script>
            """)
    List<Lead> selectList(@Param("keyword") String keyword,
                          @Param("status") Integer status,
                          @Param("source") String source,
                          @Param("ds") DataScope ds);

    /** 公海池：没有负责人的线索，所有人可见 */
    @Select("""
            <script>
            select l.*, pe.emp_name as pre_owner_name, d.dept_name
              from crm_lead l
              left join sys_employee pe on pe.id = l.pre_owner_id
              left join sys_dept d on d.id = l.dept_id
             where l.deleted = 0 and l.owner_id is null
             <if test="keyword != null and keyword != ''">
               and (l.lead_name like concat('%', #{keyword}, '%')
                    or l.company like concat('%', #{keyword}, '%'))
             </if>
             order by l.pool_time desc, l.id desc
            </script>
            """)
    List<Lead> selectPool(@Param("keyword") String keyword);

    @Select("select * from crm_lead where id = #{id} and deleted = 0")
    Lead selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_lead (lead_name, company, phone, source, status, owner_id, dept_id,
                                  next_time, last_follow_time, remark)
            values (#{leadName}, #{company}, #{phone}, #{source}, ifnull(#{status}, 0), #{ownerId}, #{deptId},
                    #{nextTime}, now(), #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Lead lead);

    @Update("update crm_lead set lead_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_lead
               set lead_name = #{leadName}, company = #{company}, phone = #{phone},
                   source = #{source}, status = ifnull(#{status}, 0), owner_id = #{ownerId},
                   dept_id = #{deptId}, next_time = #{nextTime}, remark = #{remark}
             where id = #{id}
            """)
    int update(Lead lead);

    @Update("update crm_lead set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    @Update("update crm_lead set status = 2, customer_id = #{customerId} where id = #{id}")
    int updateConverted(@Param("id") Long id, @Param("customerId") Long customerId);

    /** 有人跟进过就刷新一次，公海回收靠它判断活跃度 */
    @Update("update crm_lead set last_follow_time = now() where id = #{id}")
    int touchFollow(@Param("id") Long id);

    /** 领取公海线索，只有仍无主时才能领 */
    @Update("""
            update crm_lead
               set owner_id = #{userId}, dept_id = #{deptId}, status = 1,
                   last_follow_time = now(), pool_time = null
             where id = #{id} and deleted = 0 and owner_id is null
            """)
    int claim(@Param("id") Long id, @Param("userId") Long userId, @Param("deptId") Long deptId);

    /** 超期未跟进的一把收回公海，原负责人存到 pre_owner_id 便于追溯 */
    @Update("""
            update crm_lead
               set pre_owner_id = owner_id,
                   owner_id = null,
                   pool_time = now(),
                   status = 0
             where deleted = 0 and owner_id is not null and status != 2
               and last_follow_time < #{before}
            """)
    int recycleToPool(@Param("before") LocalDateTime before);

    /** 负责人离职：名下未转化的线索全部退回公海 */
    @Update("""
            update crm_lead
               set pre_owner_id = owner_id,
                   owner_id = null,
                   pool_time = now(),
                   status = 0
             where owner_id = #{ownerId} and deleted = 0 and status != 2
            """)
    int releaseByOwner(@Param("ownerId") Long ownerId);

    @Update("update crm_lead set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_lead where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
