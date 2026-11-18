package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface FollowMapper {

    /** 跟进记录页：全量分页，带业务对象名称 */
    @Select("""
            <script>
            select f.*, e.emp_name as create_by_name,
                   case f.biz_type
                     when 'lead' then (select lead_name from crm_lead where id = f.biz_id)
                     when 'customer' then (select customer_name from crm_customer where id = f.biz_id)
                     when 'opportunity' then (select opp_name from crm_opportunity where id = f.biz_id)
                   end as biz_name
              from crm_follow f
              left join sys_employee e on e.id = f.create_by
             where 1 = 1
             <if test="bizType != null and bizType != ''"> and f.biz_type = #{bizType} </if>
             <if test="keyword != null and keyword != ''"> and f.content like concat('%', #{keyword}, '%') </if>
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
             order by f.id desc
            </script>
            """)
    List<Follow> selectPage(@Param("keyword") String keyword,
                            @Param("bizType") String bizType,
                            @Param("ds") DataScope ds);

    @Select("""
            select f.*, e.emp_name as create_by_name
              from crm_follow f
              left join sys_employee e on e.id = f.create_by
             where f.biz_type = #{bizType} and f.biz_id = #{bizId}
             order by f.id desc
            """)
    List<Follow> selectByBiz(@Param("bizType") String bizType, @Param("bizId") Long bizId);

    @Insert("""
            insert into crm_follow (biz_type, biz_id, content, next_time, create_by)
            values (#{bizType}, #{bizId}, #{content}, #{nextTime}, #{createBy})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Follow follow);

    @Delete("delete from crm_follow where id = #{id}")
    int deleteById(@Param("id") Long id);
}
