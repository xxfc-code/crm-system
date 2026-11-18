package com.crm.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 业务里 throw new BizException("提示语")，统一转成 Result 返回给前端
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<Void> handleBiz(BizException e) {
        return Result.fail(e.getMessage());
    }

    /**
     * 其余的（空指针、SQL 报错等）不把堆栈和 SQL 抛给前端，只记日志
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleOther(Exception e) {
        log.error("接口异常", e);
        return Result.fail("操作失败了，请稍后再试");
    }
}
