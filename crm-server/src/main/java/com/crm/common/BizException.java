package com.crm.common;

/**
 * 业务异常：message 会原样返回给前端，所以要写人话
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
