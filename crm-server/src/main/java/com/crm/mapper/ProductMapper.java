package com.crm.mapper;

import com.crm.entity.Product;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProductMapper {

    @Select("""
            <script>
            select p.*, c.category_name
              from crm_product p
              left join crm_product_category c on c.id = p.category_id
             where p.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (p.product_name like concat('%', #{keyword}, '%')
                    or p.product_no like concat('%', #{keyword}, '%'))
             </if>
             <if test="categoryId != null"> and p.category_id = #{categoryId} </if>
             <if test="status != null"> and p.status = #{status} </if>
             order by p.category_id, p.id
            </script>
            """)
    List<Product> selectList(@Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("status") Integer status);

    /** 下拉选项，只要在售的 */
    @Select("select id, product_no, product_name, price, cost, unit, category_id from crm_product where deleted = 0 and status = 1 order by category_id, id")
    List<Product> selectOptions();

    @Select("select * from crm_product where id = #{id} and deleted = 0")
    Product selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_product (product_name, category_id, spec, unit, price, cost, stock, status, remark)
            values (#{productName}, #{categoryId}, #{spec}, #{unit}, ifnull(#{price}, 0),
                    ifnull(#{cost}, 0), ifnull(#{stock}, 0), ifnull(#{status}, 1), #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Product product);

    @Update("update crm_product set product_no = #{no} where id = #{id}")
    int updateNo(@Param("id") Long id, @Param("no") String no);

    @Update("""
            update crm_product
               set product_name = #{productName}, category_id = #{categoryId}, spec = #{spec}, unit = #{unit},
                   price = ifnull(#{price}, 0), cost = ifnull(#{cost}, 0), stock = ifnull(#{stock}, 0),
                   status = ifnull(#{status}, 1), remark = #{remark}
             where id = #{id}
            """)
    int update(Product product);

    /** 出库扣库存，库存不够时影响行数为 0 */
    @Update("update crm_product set stock = stock - #{qty} where id = #{productId} and stock >= #{qty}")
    int deductStock(@Param("productId") Long productId, @Param("qty") Integer qty);

    /** 退货回补库存 */
    @Update("update crm_product set stock = stock + #{qty} where id = #{productId}")
    int restoreStock(@Param("productId") Long productId, @Param("qty") Integer qty);

    @Update("update crm_product set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_product where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
