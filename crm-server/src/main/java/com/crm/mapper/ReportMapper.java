package com.crm.mapper;

import com.crm.common.DataScope;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface ReportMapper {

    /** 近 12 个月按签约月汇总订单 */
    @Select("""
            <script>
            select date_format(o.sign_date, '%Y-%m') as month,
                   count(*) as orderCount,
                   ifnull(sum(o.amount), 0) as orderAmount
              from crm_order o
             where o.deleted = 0 and o.sign_date is not null
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
             group by month order by month desc limit 12
            </script>
            """)
    List<Map<String, Object>> salesByMonth(@Param("ds") DataScope ds);

    /** 近 12 个月按回款月汇总 */
    @Select("""
            <script>
            select date_format(p.pay_date, '%Y-%m') as month, ifnull(sum(p.amount), 0) as payAmount
              from crm_payment p
             where p.deleted = 0 and p.pay_date is not null
             <if test="ds != null and ds.scope == 4"> and p.owner_id = #{ds.userId} </if>
             <if test="ds != null and (ds.scope == 2 or ds.scope == 3)">
               <choose>
                 <when test="ds.deptIds != null and ds.deptIds.size() > 0">
                   and p.dept_id in
                   <foreach collection="ds.deptIds" item="d" open="(" separator="," close=")">#{d}</foreach>
                 </when>
                 <otherwise>and 1 = 0</otherwise>
               </choose>
             </if>
             group by month order by month desc limit 12
            </script>
            """)
    List<Map<String, Object>> paymentByMonth(@Param("ds") DataScope ds);

    /** 客户采购排行 */
    @Select("""
            <script>
            select c.id, c.customer_name as customerName, c.level,
                   count(o.id) as orderCount,
                   ifnull(sum(o.amount), 0) as totalAmount
              from crm_customer c
              left join crm_order o on o.customer_id = c.id and o.deleted = 0 and o.status != '已取消'
             where c.deleted = 0
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
             group by c.id, c.customer_name, c.level
             order by totalAmount desc
             limit 20
            </script>
            """)
    List<Map<String, Object>> customerPurchase(@Param("ds") DataScope ds);

    /** 产品销量按报价明细口径统计 */
    @Select("""
            <script>
            select qi.product_name as productName,
                   count(distinct qi.quotation_id) as quoteCount,
                   ifnull(sum(qi.qty), 0) as qty,
                   ifnull(sum(qi.amount), 0) as amount
              from crm_quotation_item qi
              join crm_quotation q on q.id = qi.quotation_id and q.deleted = 0
             where 1 = 1
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
             group by qi.product_name
             order by amount desc
            </script>
            """)
    List<Map<String, Object>> productSales(@Param("ds") DataScope ds);

    /** 应收账款：订单金额减去已收款 */
    @Select("""
            <script>
            select o.id, o.order_no as orderNo, c.customer_name as customerName,
                   e.emp_name as ownerName, o.amount as total,
                   ifnull(p.paid, 0) as paid,
                   (o.amount - ifnull(p.paid, 0)) as remain,
                   o.sign_date as signDate, o.status
              from crm_order o
              left join crm_customer c on c.id = o.customer_id
              left join sys_employee e on e.id = o.owner_id
              left join (select order_id, sum(amount) paid from crm_payment where deleted = 0 group by order_id) p
                on p.order_id = o.id
             where o.deleted = 0 and o.status != '已取消'
               and (o.amount - ifnull(p.paid, 0)) &gt; 0
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
             order by remain desc
            </script>
            """)
    List<Map<String, Object>> receivable(@Param("ds") DataScope ds);

    /** 待处理工单 */
    @Select("""
            <script>
            select t.id, t.ticket_no as ticketNo, t.title, t.priority, t.status,
                   c.customer_name as customerName, t.create_time as createTime
              from crm_ticket t
              left join crm_customer c on c.id = t.customer_id
             where t.deleted = 0 and t.status in ('待受理', '处理中')
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
             order by t.id desc limit 5
            </script>
            """)
    List<Map<String, Object>> todoTickets(@Param("ds") DataScope ds);

    /** 待处理退换货 */
    @Select("""
            <script>
            select r.id, r.return_no as returnNo, r.return_type as returnType, r.reason, r.amount,
                   c.customer_name as customerName
              from crm_return_order r
              left join crm_customer c on c.id = r.customer_id
             where r.deleted = 0 and r.status = '待处理'
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
             order by r.id desc limit 5
            </script>
            """)
    List<Map<String, Object>> todoReturns(@Param("ds") DataScope ds);
}
