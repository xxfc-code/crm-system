package com.crm.mapper;

import com.crm.entity.Menu;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface MenuMapper {

    @Select("select * from sys_menu where status = 1 order by parent_id, sort")
    List<Menu> selectAll();

    @Select("""
            select m.* from sys_menu m
              join sys_role_menu rm on rm.menu_id = m.id
             where rm.role_id = #{roleId} and m.status = 1
             order by m.parent_id, m.sort
            """)
    List<Menu> selectByRoleId(@Param("roleId") Long roleId);

    /** 权限管理页用，包含停用的 */
    @Select("select * from sys_menu order by parent_id, sort")
    List<Menu> selectAllForManage();

    @Select("select * from sys_menu where id = #{id}")
    Menu selectById(@Param("id") Long id);

    @Select("select count(*) from sys_menu where parent_id = #{id}")
    int countChildren(@Param("id") Long id);

    @Select("select count(*) from sys_role_menu where menu_id = #{id}")
    int countRoleRef(@Param("id") Long id);

    @Insert("""
            insert into sys_menu (parent_id, menu_name, menu_type, path, component, perms, icon, sort, status)
            values (ifnull(#{parentId}, 0), #{menuName}, #{menuType}, #{path}, #{component}, #{perms},
                    #{icon}, ifnull(#{sort}, 0), ifnull(#{status}, 1))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Menu menu);

    @Update("""
            update sys_menu
               set parent_id = ifnull(#{parentId}, 0), menu_name = #{menuName}, menu_type = #{menuType},
                   path = #{path}, component = #{component}, perms = #{perms}, icon = #{icon},
                   sort = ifnull(#{sort}, 0), status = ifnull(#{status}, 1)
             where id = #{id}
            """)
    int update(Menu menu);

    @Delete("delete from sys_menu where id = #{id}")
    int deleteById(@Param("id") Long id);
}
