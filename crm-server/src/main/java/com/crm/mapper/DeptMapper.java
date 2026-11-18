package com.crm.mapper;

import com.crm.entity.Dept;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DeptMapper {

    /** 一次捞全部未删除部门，交给内存组树 */
    @Select("select * from sys_dept where deleted = 0 order by parent_id, sort")
    List<Dept> selectAll();

    @Select("select * from sys_dept where id = #{id} and deleted = 0")
    Dept selectById(@Param("id") Long id);

    @Select("select count(*) from sys_dept where parent_id = #{parentId} and deleted = 0")
    int countChildren(@Param("parentId") Long parentId);

    @Select("select count(*) from sys_employee where dept_id = #{deptId} and deleted = 0")
    int countEmployees(@Param("deptId") Long deptId);

    @Insert("""
            insert into sys_dept (parent_id, dept_name, leader, phone, sort, status)
            values (#{parentId}, #{deptName}, #{leader}, #{phone},
                    ifnull(#{sort}, 0), ifnull(#{status}, 1))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Dept dept);

    @Update("""
            update sys_dept
               set parent_id = #{parentId},
                   dept_name = #{deptName},
                   leader    = #{leader},
                   phone     = #{phone},
                   sort      = ifnull(#{sort}, 0),
                   status    = ifnull(#{status}, 1)
             where id = #{id}
            """)
    int update(Dept dept);

    @Update("update sys_dept set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from sys_dept where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
