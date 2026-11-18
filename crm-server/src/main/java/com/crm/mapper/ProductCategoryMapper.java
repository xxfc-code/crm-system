package com.crm.mapper;

import com.crm.entity.ProductCategory;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ProductCategoryMapper {

    @Select("""
            select c.*, (select count(*) from crm_product p where p.category_id = c.id and p.deleted = 0) as product_count
              from crm_product_category c
             where c.deleted = 0
             order by c.sort, c.id
            """)
    List<ProductCategory> selectList();

    @Select("select * from crm_product_category where id = #{id} and deleted = 0")
    ProductCategory selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_product_category (category_name, parent_id, sort, status)
            values (#{categoryName}, ifnull(#{parentId}, 0), ifnull(#{sort}, 0), ifnull(#{status}, 1))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(ProductCategory category);

    @Update("""
            update crm_product_category
               set category_name = #{categoryName}, sort = ifnull(#{sort}, 0), status = ifnull(#{status}, 1)
             where id = #{id}
            """)
    int update(ProductCategory category);

    @Select("select count(*) from crm_product where category_id = #{id} and deleted = 0")
    int countProducts(@Param("id") Long id);

    @Update("update crm_product_category set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_product_category where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
