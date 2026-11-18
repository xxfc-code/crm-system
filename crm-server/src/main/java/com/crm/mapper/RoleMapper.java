package com.crm.mapper;

import com.crm.entity.Role;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface RoleMapper {

    @Select("select * from sys_role where deleted = 0 order by sort")
    List<Role> selectList();

    @Select("select * from sys_role where id = #{id} and deleted = 0")
    Role selectById(@Param("id") Long id);

    @Select("select count(*) from sys_employee where role_id = #{roleId} and deleted = 0")
    int countEmployees(@Param("roleId") Long roleId);

    @Insert("""
            insert into sys_role (role_name, role_key, sort, status, data_scope, remark)
            values (#{roleName}, #{roleKey}, ifnull(#{sort}, 0), ifnull(#{status}, 1),
                    ifnull(#{dataScope}, 4), #{remark})
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Role role);

    @Update("""
            update sys_role
               set role_name = #{roleName}, role_key = #{roleKey},
                   sort = ifnull(#{sort}, 0), status = ifnull(#{status}, 1),
                   data_scope = ifnull(#{dataScope}, 4), remark = #{remark}
             where id = #{id}
            """)
    int update(Role role);

    @Update("update sys_role set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from sys_role where id = #{id}")
    int deleteForce(@Param("id") Long id);

    @Select("select menu_id from sys_role_menu where role_id = #{roleId}")
    List<Long> selectMenuIds(@Param("roleId") Long roleId);

    @Delete("delete from sys_role_menu where role_id = #{roleId}")
    int deleteRoleMenu(@Param("roleId") Long roleId);

    @Insert("insert into sys_role_menu (role_id, menu_id) values (#{roleId}, #{menuId})")
    int insertRoleMenu(@Param("roleId") Long roleId, @Param("menuId") Long menuId);
}
