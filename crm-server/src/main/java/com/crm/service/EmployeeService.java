package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.PageResult;
import com.crm.common.UserContext;
import com.crm.entity.Employee;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.LeadMapper;
import com.github.pagehelper.PageHelper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class EmployeeService {

    /** 新员工和重置密码统一用这个初始密码 */
    private static final String INIT_PASSWORD = "123456";

    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private LeadMapper leadMapper;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public PageResult<Employee> page(int pageNum, int pageSize, Long deptId, String keyword) {
        PageHelper.startPage(pageNum, pageSize);
        return PageResult.of(employeeMapper.selectList(deptId, keyword));
    }

    /** 下拉选择用，不分页；人员规模大了要改成按关键字搜 */
    public List<Employee> options() {
        return employeeMapper.selectList(null, null);
    }

    /**
     * 先插入拿自增ID，再回填 EMP0001 形式的工号，工号只依赖主键，不可能重复
     */
    public void add(Employee employee) {
        employee.setPassword(encoder.encode(INIT_PASSWORD));
        employeeMapper.insert(employee);
        employeeMapper.updateEmpNo(employee.getId(), "EMP" + String.format("%04d", employee.getId()));
    }

    public void update(Employee employee) {
        employeeMapper.update(employee);
        // 离职：名下未转化的线索退回公海，避免跟着人一起"失联"
        if (employee.getStatus() != null && employee.getStatus() == 0) {
            int released = leadMapper.releaseByOwner(employee.getId());
            if (released > 0) {
                log.info("员工 {} 离职，{} 条线索退回公海", employee.getEmpName(), released);
            }
        }
    }

    public void resetPassword(Long id) {
        employeeMapper.updatePassword(id, encoder.encode(INIT_PASSWORD));
    }

    public void delete(Long id, boolean force) {
        if (id.equals(UserContext.get())) {
            throw new BizException("不能删除自己");
        }
        if (force) {
            employeeMapper.deleteForce(id);
        } else {
            employeeMapper.deleteLogic(id);
        }
    }
}
