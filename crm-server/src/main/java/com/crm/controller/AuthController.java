package com.crm.controller;

import com.crm.common.Result;
import com.crm.entity.Employee;
import com.crm.service.AuthService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    public record LoginParam(String empNo, String password) {
    }

    /** 改密码用，两个字段都得有 */
    public record PasswordParam(String oldPassword, String newPassword) {
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginParam param) {
        return Result.ok(authService.login(param.empNo(), param.password()));
    }

    /** 当前登录用户的资料 */
    @GetMapping("/profile")
    public Result<Employee> profile() {
        return Result.ok(authService.profile());
    }

    /**
     * 修改个人资料：只改姓名/性别/手机/邮箱
     * 不需要额外权限 —— 改自己的资料是每个登录用户天然该有的能力
     */
    @PutMapping("/profile")
    public Result<Employee> updateProfile(@RequestBody Employee form) {
        return Result.ok(authService.updateProfile(form));
    }

    /** 修改密码，改完旧 token 失效，需要重新登录 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordParam param) {
        authService.changePassword(param.oldPassword(), param.newPassword());
        return Result.ok();
    }
}
