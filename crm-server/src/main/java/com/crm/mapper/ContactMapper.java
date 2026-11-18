package com.crm.mapper;

import com.crm.entity.Contact;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface ContactMapper {

    @Select("""
            <script>
            select ct.*, c.customer_name
              from crm_contact ct
              left join crm_customer c on c.id = ct.customer_id
             where ct.deleted = 0
             <if test="keyword != null and keyword != ''">
               and (ct.contact_name like concat('%', #{keyword}, '%')
                    or ct.phone like concat('%', #{keyword}, '%')
                    or c.customer_name like concat('%', #{keyword}, '%'))
             </if>
             <if test="customerId != null"> and ct.customer_id = #{customerId} </if>
             order by ct.is_primary desc, ct.id desc
            </script>
            """)
    List<Contact> selectList(@Param("keyword") String keyword, @Param("customerId") Long customerId);

    @Select("select * from crm_contact where id = #{id} and deleted = 0")
    Contact selectById(@Param("id") Long id);

    @Insert("""
            insert into crm_contact (customer_id, contact_name, position, phone, email, wechat, is_primary, remark)
            values (#{customerId}, #{contactName}, #{position}, #{phone}, #{email}, #{wechat},
                    ifnull(#{isPrimary}, 0), #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Contact contact);

    @Update("""
            update crm_contact
               set customer_id = #{customerId}, contact_name = #{contactName}, position = #{position},
                   phone = #{phone}, email = #{email}, wechat = #{wechat},
                   is_primary = ifnull(#{isPrimary}, 0), remark = #{remark}
             where id = #{id}
            """)
    int update(Contact contact);

    @Update("update crm_contact set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from crm_contact where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
