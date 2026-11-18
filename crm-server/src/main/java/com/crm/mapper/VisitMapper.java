package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Visit;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface VisitMapper {

    @Select("""
            <script>
            select v.*, c.customer_name, e.emp_name as visitor_name
              from crm_visit v
              left join crm_customer c on c.id = v.customer_id
              left join sys_employee e on e.id = v.visitor_id
             where v.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (c.customer_name like concat('%', #{keyword}, '%')
                    or v.content like concat('%', #{keyword}, '%'))
             </if>
             <if test="way != null and way != ''"> and v.way = #{way} </if>
             <if test="ds != null and ds.scope == 4"> and v.visitor_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and e.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by v.id desc
            </script>
            """)
    List<Visit> selectList(@Param("keyword") String keyword,
                           @Param("way") String way,
                           @Param("ds") DataScope ds);

    @Select("select * from crm_visit where id = #{id} and deleted = 0")
    Visit selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_visit (customer_id, visit_date, visitor_id, way, content, satisfaction, next_time, remark)
            values (#{customerId}, #{visitDate}, #{visitorId}, ifnull(#{way}, '电话'), #{content},
                    ifnull(#{satisfaction}, 5), #{nextTime}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Visit visit);

    @Update("""
            update crm_visit
               set customer_id = #{customerId}, visit_date = #{visitDate}, visitor_id = #{visitorId},
                   way = #{way}, content = #{content}, satisfaction = ifnull(#{satisfaction}, 5),
                   next_time = #{nextTime}, remark = #{remark}
             where id = #{id}
            """)
    int update(Visit visit);

    @Update("update crm_visit set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_visit where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
