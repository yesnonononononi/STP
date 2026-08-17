package com.summit.stp.common.config.global;

import com.summit.stp.common.application.domain.exception.AuthException;
import com.summit.stp.common.application.domain.exception.BusinessException;
import com.summit.stp.common.application.api.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Result<Void>> handleAuthException(AuthException e) {
        log.warn("【鉴权】{}", e.getMessage());
        return ResponseEntity.status(e.getStatus()).body(Result.error(e.getStatus(), e.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("【系统】参数校验失败", e);
        return Result.error("参数异常");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        log.warn("【系统】请求体反序列化失败: {}", e.getMessage());
        return Result.error("请求参数格式错误");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        log.warn("【文件服务】文件大小超出限制: {}", e.getMessage());
        return Result.error("文件大小超出限制，请上传不超过50MB的文件");
    }

    @ExceptionHandler(ClientAbortException.class)
    public void handleClientAbortException(ClientAbortException e) {
        log.debug("【网络】客户端中断连接: {}", e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("【系统】未捕获异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }
}
