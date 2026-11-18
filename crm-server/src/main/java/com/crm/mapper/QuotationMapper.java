package com.crm.mapper;

import com.crm.common.DataScope;
import com.crm.entity.Quotation;
import com.crm.entity.QuotationItem;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface QuotationMapper {

    @Select("""
            <script>
            select q.*, c.customer_name, e.emp_name as owner_name,
                   (case when q.valid_until is not null and q.valid_until &lt; curdate() then 1 else 0 end) as expired,
                   (select count(*) from crm_order o where o.quote_id = q.id and o.deleted = 0) as converted_count
              from crm_quotation q
              left join crm_customer c on c.id = q.customer_id
              left join sys_employee e on e.id = q.owner_id
             where q.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (q.quote_no like concat('%', #{keyword}, '%') or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="status != null and status != ''"> and q.status = #{status} </if>
             <if test="ds != null and ds.scope == 4"> and q.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and q.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             order by q.id desc
            </script>
            """)
    List<Quotation> selectList(@Param("keyword") String keyword,
                               @Param("status") String status,
                               @Param("ds") DataScope ds);

    @Select("select * from crm_quotation where id = #{id} and deleted = 0")
    Quotation selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_quotation (customer_id, opp_id, quote_date, valid_until, total_amount, status,
                                       approve_status, owner_id, dept_id, remark)
            values (#{customerId}, #{oppId}, #{quoteDate}, #{validUntil}, ifnull(#{totalAmount}, 0),
                    ifnull(#{status}, '草稿'), ifnull(#{approveStatus}, 0), #{ownerId}, #{deptId}, #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Quotation quotation);

    @Update("update crm_quotation set quote_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_quotation
               set customer_id = #{customerId}, opp_id = #{oppId}, quote_date = #{quoteDate},
                   valid_until = #{validUntil}, total_amount = ifnull(#{totalAmount}, 0),
                   status = ifnull(#{status}, '草稿'), approve_status = ifnull(#{approveStatus}, 0),
                   owner_id = #{ownerId}, remark = #{remark}
             where id = #{id}
            """)
    int update(Quotation quotation);

    @Update("update crm_quotation set status = #{status} where id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") String status);

    /** 审批：2通过 3驳回 */
    @Update("""
            update crm_quotation
               set approve_status = #{approveStatus}, approve_user_id = #{approveUserId},
                   approve_time = now(), approve_remark = #{remark}
             where id = #{id}
            """)
    int updateApprove(@Param("id") Long id,
                      @Param("approveStatus") Integer approveStatus,
                      @Param("approveUserId") Long approveUserId,
                      @Param("remark") String remark);

    @Update("update crm_quotation set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_quotation where id = #{id}")
    int deleteForce(@Param("id") Long id);

    @Select("select * from crm_quotation_item where quotation_id = #{quotationId} order by id")
    List<QuotationItem> selectItems(@Param("quotationId") Long quotationId);

    @Insert("""
            insert into crm_quotation_item (quotation_id, product_id, product_name, qty, price, cost, amount)
            values (#{quotationId}, #{productId}, #{productName}, #{qty}, #{price}, #{cost}, #{amount})
            """)
    int insertItem(QuotationItem item);

    @Delete("delete from crm_quotation_item where quotation_id = #{quotationId}")
    int deleteItems(@Param("quotationId") Long quotationId);
}
