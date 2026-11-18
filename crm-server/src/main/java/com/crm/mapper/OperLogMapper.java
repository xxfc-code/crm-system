package com.crm.mapper;

import com.crm.entity.OperLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface OperLogMapper {

    @Insert("""
            insert into sys_oper_log (oper_user_id, oper_user_name, module, operation, method, params, ip, cost_ms, success, error_msg)
            values (#{operUserId}, #{operUserName}, #{module}, #{operation}, #{method}, #{params}, #{ip},
                    #{costMs}, #{success}, #{errorMsg})
            """)
    int insert(OperLog log);

    @Select("""
            <script>
            select * from sys_oper_log
             where 1 = 1
             <if test="keyword != null and keyword != ''">
               and (oper_user_name like concat('%', #{keyword}, '%') or method like concat('%', #{keyword}, '%'))
             </if>
             <if test="module != null and module != ''"> and module = #{module} </if>
             order by id desc
            </script>
            """)
    List<OperLog> selectList(@Param("keyword") String keyword, @Param("module") String module);
}
