package com.crm.mapper;

import com.crm.entity.Employee;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface EmployeeMapper {

    /** 登录用，带 password */
    @Select("select * from sys_employee where emp_no = #{empNo} and deleted = 0")
    Employee selectByEmpNo(@Param("empNo") String empNo);

    @Select("select * from sys_employee where id = #{id} and deleted = 0")
    Employee selectById(@Param("id") Long id);

    /** 详情用，连表带出部门和角色名，不带 password */
    @Select("""
            select e.id, e.emp_no, e.emp_name, e.dept_id, e.role_id, e.phone, e.email,
                   e.gender, e.status, e.create_time, e.update_time, d.dept_name, r.role_name
              from sys_employee e
              left join sys_dept d on d.id = e.dept_id
              left join sys_role r on r.id = e.role_id
             where e.id = #{id} and e.deleted = 0
            """)
    Employee selectDetailById(@Param("id") Long id);

    /** 列表不带 password */
    @Select("""
            <script>
            select e.id, e.emp_no, e.emp_name, e.dept_id, e.role_id, e.phone, e.email,
                   e.gender, e.status, e.create_time, d.dept_name, r.role_name
              from sys_employee e
              left join sys_dept d on d.id = e.dept_id
              left join sys_role r on r.id = e.role_id
             where e.deleted = 0
             <if test="deptId != null"> and e.dept_id = #{deptId} </if>
             <if test="keyword != null and keyword != ''">
               and (e.emp_name like concat('%', #{keyword}, '%') or e.emp_no like concat('%', #{keyword}, '%'))
             </if>
             order by e.id
            </script>
            """)
    List<Employee> selectList(@Param("deptId") Long deptId, @Param("keyword") String keyword);

    @Insert("""
            insert into sys_employee (emp_name, password, dept_id, role_id, phone, email, gender, status)
            values (#{empName}, #{password}, #{deptId}, #{roleId}, #{phone}, #{email},
                    ifnull(#{gender}, 0), ifnull(#{status}, 1))
            """)
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Employee employee);

    @Update("update sys_employee set emp_no = #{empNo} where id = #{id}")
    int updateEmpNo(@Param("id") Long id, @Param("empNo") String empNo);

    @Update("""
            update sys_employee
               set emp_name = #{empName}, dept_id = #{deptId}, role_id = #{roleId},
                   phone = #{phone}, email = #{email},
                   gender = ifnull(#{gender}, 0), status = ifnull(#{status}, 1)
             where id = #{id}
            """)
    int update(Employee employee);

    @Update("update sys_employee set password = #{password} where id = #{id}")
    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /**
     * 个人资料专用更新：只动姓名/手机/邮箱/性别四个字段
     * 部门、角色、状态、工号都不在这里 —— 它们归管理员管，前端传了也改不动
     */
    @Update("""
            update sys_employee
               set emp_name = #{empName}, phone = #{phone}, email = #{email},
                   gender = ifnull(#{gender}, 0)
             where id = #{id}
            """)
    int updateProfile(Employee employee);

    @Update("update sys_employee set deleted = 1 where id = #{id}")
    int deleteLogic(@Param("id") Long id);

    @Delete("delete from sys_employee where id = #{id}")
    int deleteForce(@Param("id") Long id);
}
