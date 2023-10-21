package com.louis.user_center.exception;

import com.louis.user_center.common.BaseResponse;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.BindException;

/**
 * 全局异常处理器 通过日志的形式记录并且集中处理报错信息
 * 通过注解@RestControllerAdvice  @ExceptionHandler(BusinessException.class) @ExceptionHandler(RuntimeException.class) 标记
 * 捕捉抛出的异常(无论是controller层 还是service层) 然后通过注解@Slf4j 记录下来 同时返回给前端公共响应类(BaseResponse)（含有错误信息的异常全局响应类）
 */
@RestControllerAdvice
@Slf4j
// 两种异常类 自定义BusinessEX业务异常类 默认系统异常类RuntimeEX
public class GlobalExceptionHandler {
    //锁定标记文件class
    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        log.error("BusinessException"+e.getMessage(),e);
        return ResultUtils.error(e.getCode(),e.getMessage(),e.getDescription());
    }
    @ExceptionHandler(RuntimeException.class)
    public BaseResponse runtimeExceptionHandler(RuntimeException e){
        //日志记录报错信息
        log.error("runtimeException",e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }
}
