package com.crm.service;

import com.crm.common.BizException;
import com.crm.common.JwtUtil;
import com.crm.common.UserContext;
import com.crm.entity.Employee;
import com.crm.entity.Menu;
import com.crm.entity.Role;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.MenuMapper;
import com.crm.mapper.RoleMapper;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {

    /** 手机号：1 开头 11 位 */
    private static final Pattern PHONE = Pattern.compile("^1[3-9]\\d{9}$");
    /** 邮箱：够用即可，不追求 RFC 全套 */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private RoleMapper roleMapper;
    @Resource
    private MenuMapper menuMapper;
    @Resource
    private MenuService menuService;
    @Resource
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    /**
     * 登录成功一次性把 token、用户信息、菜单树、权限标识全给前端，省掉后续两次请求
     */
    public Map<String, Object> login(String empNo, String password) {
        Employee emp = employeeMapper.selectByEmpNo(empNo);
        if (emp == null || !encoder.matches(password, emp.getPassword())) {
            throw new BizException("工号或密码错误");
        }
        if (emp.getStatus() == 0) {
            throw new BizException("该员工已离职，无法登录");
        }

        Role role = roleMapper.selectById(emp.getRoleId());
        String roleKey = role == null ? "" : role.getRoleKey();

        // 超级管理员直接拿全量菜单，其余按角色授权查
        List<Menu> granted = "admin".equals(roleKey)
                ? menuMapper.selectAll()
                : menuMapper.selectByRoleId(emp.getRoleId());

        // 菜单只保留目录和菜单，按钮不进导航；权限标识要把按钮也算进去
        List<Menu> menus = menuService.buildTree(
                granted.stream().filter(m -> !"F".equals(m.getMenuType())).toList());
        List<String> perms = granted.stream()
                .map(Menu::getPerms)
                .filter(p -> p != null && !p.isBlank())
                .toList();

        String fingerprint = JwtUtil.fingerprint(emp.getPassword());
        emp.setPassword(null);
        emp.setRoleName(role == null ? null : role.getRoleName());

        Map<String, Object> data = new HashMap<>();
        data.put("token", jwtUtil.create(emp.getId(), emp.getEmpNo(), fingerprint));
        data.put("user", emp);
        data.put("menus", menus);
        data.put("perms", perms);
        return data;
    }

    /**
     * 当前登录用户的资料
     * 实时查库而不是读 token 里的缓存，这样管理员刚调过部门角色，这里就能看到最新值
     */
    public Employee profile() {
        Employee emp = employeeMapper.selectDetailById(UserContext.get());
        if (emp == null) {
            throw new BizException("账号不存在");
        }
        return emp;
    }

    /**
     * 修改个人资料
     *
     * 三条防线：
     * 1. 不收 id，操作对象只能是 UserContext 里那个当前登录用户 —— 传别人的 id 也改不到别人
     * 2. 只更新姓名/性别/手机/邮箱，部门、角色、状态、工号前端传了也进不了 SQL
     * 3. 每个字段单独校验格式，不合法直接给中文提示
     */
    public Employee updateProfile(Employee form) {
        Long id = UserContext.get();
        if (employeeMapper.selectById(id) == null) {
            throw new BizException("账号不存在");
        }

        String empName = trim(form.getEmpName());
        if (empName.isEmpty()) {
            throw new BizException("姓名不能为空");
        }
        if (empName.length() > 30) {
            throw new BizException("姓名不能超过 30 个字");
        }

        String phone = trim(form.getPhone());
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) {
            throw new BizException("手机号格式不正确，请填写 11 位手机号");
        }

        String email = trim(form.getEmail());
        if (!email.isEmpty() && !EMAIL.matcher(email).matches()) {
            throw new BizException("邮箱格式不正确");
        }

        Integer gender = form.getGender() == null ? 0 : form.getGender();
        if (gender < 0 || gender > 2) {
            throw new BizException("性别取值不合法");
        }

        Employee upd = new Employee();
        upd.setId(id);
        upd.setEmpName(empName);
        upd.setPhone(phone);
        upd.setEmail(email);
        upd.setGender(gender);
        employeeMapper.updateProfile(upd);

        return profile();
    }

    /**
     * 修改密码
     *
     * 必须先验原密码，防止拿着 token 直接把密码改掉把账号锁在门外
     * 改完密码指纹就变了，签发出去的旧 token 会自动失效（AuthInterceptor 里校验）
     */
    public void changePassword(String oldPassword, String newPassword) {
        Employee emp = employeeMapper.selectById(UserContext.get());
        if (emp == null) {
            throw new BizException("账号不存在");
        }
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BizException("请输入当前密码");
        }
        if (!encoder.matches(oldPassword, emp.getPassword())) {
            throw new BizException("当前密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        if (newPassword.length() > 32) {
            throw new BizException("新密码不能超过 32 位");
        }
        if (oldPassword.equals(newPassword)) {
            throw new BizException("新密码不能和当前密码一样");
        }
        employeeMapper.updatePassword(emp.getId(), encoder.encode(newPassword));
    }

    private String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
