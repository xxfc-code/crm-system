package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Opportunity;
import com.crm.entity.OpportunityStageLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface OpportunityMapper {

    @Select("""
            <script>
            select o.*, c.customer_name, e.emp_name as owner_name
              from crm_opportunity o
              left join crm_customer c on c.id = o.customer_id
              left join sys_employee e on e.id = o.owner_id
             where o.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (o.opp_name like concat('%', #{keyword}, '%')
                    or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="stage != null and stage != ''"> and o.stage = #{stage} </if>
             <if test="customerId != null"> and o.customer_id = #{customerId} </if>
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
    List<Opportunity> selectList(@Param("keyword") String keyword,
                                 @Param("stage") String stage,
                                 @Param("customerId") Long customerId,
                                 @Param("ds") DataScope ds);

    @Select("select * from crm_opportunity where id = #{id} and deleted = 0")
    Opportunity selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_opportunity (opp_name, customer_id, amount, stage, win_rate,
                                         expect_date, owner_id, dept_id, remark)
            values (#{oppName}, #{customerId}, ifnull(#{amount}, 0), ifnull(#{stage}, '初步接洽'),
                    ifnull(#{winRate}, 10), #{expectDate}, #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Opportunity opportunity);

    @Update("update crm_opportunity set opp_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_opportunity
               set opp_name = #{oppName}, customer_id = #{customerId}, amount = ifnull(#{amount}, 0),
                   stage = ifnull(#{stage}, '初步接洽'), win_rate = ifnull(#{winRate}, 10),
                   expect_date = #{expectDate}, owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(Opportunity opportunity);

    @Update("update crm_opportunity set stage = #{stage}, win_rate = #{winRate} where id = #{id}")
    int updateStage(@Param("id") Long id, @Param("stage") String stage, @Param("winRate") Integer winRate);

    /** 报价确认后把金额回写到商机 */
    @Update("update crm_opportunity set amount = #{amount} where id = #{id} and deleted = 0")
    int updateAmount(@Param("id") Long id, @Param("amount") java.math.BigDecimal amount);

    @Insert("""
            insert into crm_opportunity_stage_log (opportunity_id, from_stage, to_stage, win_rate, operator_id, remark)
            values (#{opportunityId}, #{fromStage}, #{toStage}, #{winRate}, #{operatorId}, #{remark})
            """)
    int insertStageLog(OpportunityStageLog log);

    @Select("""
            select l.*, e.emp_name as operator_name
              from crm_opportunity_stage_log l
              left join sys_employee e on e.id = l.operator_id
             where l.opportunity_id = #{opportunityId}
             order by l.id desc
            """)
    List<OpportunityStageLog> selectStageLogs(@Param("opportunityId") Long opportunityId);

    @Update("update crm_opportunity set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_opportunity where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
