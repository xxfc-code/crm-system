package com.crm.common;

import com.crm.entity.Employee;
import com.crm.mapper.EmployeeMapper;
import com.crm.service.DataScopeService;
import com.crm.service.PermService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private EmployeeMapper employeeMapper;
    @Resource
    private DataScopeService dataScopeService;
    @Resource
    private PermService permService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String auth = request.getHeader("Authorization");
        JwtUtil.TokenInfo info;
        try {
            info = jwtUtil.parse(auth.substring(7));
        } catch (Exception e) {
            return reject(response, 401, "登录已过期，请重新登录");
        }

        // 密码指纹对不上，说明改过密码（或账号被停用/删除），旧 token 一律作废
        if (info.fingerprint() != null && !info.fingerprint().isBlank()) {
            Employee emp = employeeMapper.selectById(info.userId());
            if (emp == null) {
                return reject(response, 401, "账号不存在或已删除，请重新登录");
            }
            if (emp.getStatus() != null && emp.getStatus() == 0) {
                return reject(response, 401, "该员工已离职，无法继续操作");
            }
            if (!info.fingerprint().equals(JwtUtil.fingerprint(emp.getPassword()))) {
                return reject(response, 401, "密码已变更，请重新登录");
            }
        }

        UserContext.set(info.userId());

        // 方法上标了 @RequiresPerm 的，在这里统一校验
        if (handler instanceof HandlerMethod method) {
            RequiresPerm required = method.getMethodAnnotation(RequiresPerm.class);
            if (required != null && !permService.has(required.value())) {
                return reject(response, 403, "没有这个操作的权限");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
        dataScopeService.clear();
        permService.clear();
    }

    private boolean reject(HttpServletResponse response, int status, String msg) throws Exception {
        response.setStatus(status);
        response.setContentType("application/json;charset=utf-8");
        response.getWriter().write("{\"code\":" + status + ",\"msg\":\"" + msg + "\"}");
        return false;
    }
}
