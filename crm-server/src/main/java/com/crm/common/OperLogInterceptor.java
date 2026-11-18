package com.crm.common;

import com.crm.entity.Employee;
import com.crm.entity.OperLog;
import com.crm.mapper.EmployeeMapper;
import com.crm.mapper.OperLogMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 记录写操作日志，方便查「谁在什么时候改了什么」
 */
@Component
public class OperLogInterceptor implements HandlerInterceptor {

    private static final String START_ATTR = "__operStart";
    private static final String USER_ATTR = "__operUser";

    @Resource
    private OperLogMapper operLogMapper;
    @Resource
    private EmployeeMapper employeeMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.currentTimeMillis());
        request.setAttribute(USER_ATTR, UserContext.get());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        try {
            String method = request.getMethod();
            if (!"POST".equals(method) && !"PUT".equals(method) && !"DELETE".equals(method)) {
                return;
            }
            Long userId = (Long) request.getAttribute(USER_ATTR);
            if (userId == null) {
                return;
            }

            String path = request.getRequestURI();
            Long start = (Long) request.getAttribute(START_ATTR);
            Employee emp = employeeMapper.selectById(userId);

            OperLog log = new OperLog();
            log.setOperUserId(userId);
            log.setOperUserName(emp == null ? null : emp.getEmpName());
            log.setModule(moduleOf(path));
            log.setOperation(operationOf(handler, method));
            log.setMethod(method + " " + path);
            log.setParams(request.getQueryString());
            log.setIp(clientIp(request));
            log.setCostMs(start == null ? 0 : System.currentTimeMillis() - start);
            log.setSuccess(ex == null && response.getStatus() < 400 ? 1 : 0);
            log.setErrorMsg(ex == null ? null : ex.getMessage());
            operLogMapper.insert(log);
        } catch (Exception ignore) {
            // 写日志失败不能影响主流程
        }
    }

    /** /api/lead/list -> lead */
    private String moduleOf(String path) {
        String[] parts = path.split("/");
        return parts.length > 2 ? parts[2] : "other";
    }

    /** 优先用接口上标的权限标识当操作名 */
    private String operationOf(Object handler, String method) {
        if (handler instanceof HandlerMethod hm) {
            RequiresPerm perm = hm.getMethodAnnotation(RequiresPerm.class);
            if (perm != null) {
                return perm.value();
            }
            return hm.getMethod().getName();
        }
        return method;
    }

    private String clientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
