package cn.hjw.dev.platform.app.config;

import cn.hjw.dev.platform.api.response.Response;
import cn.hjw.dev.platform.types.enums.ResponseCode;
import cn.hjw.dev.platform.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理自定义业务异常 (AppException)
     * 比如：未登录、库存不足、参数校验失败等
     */
    @ExceptionHandler(AppException.class)
    public Response<String> handleAppException(AppException e) {
        log.warn("业务异常捕获 code:{} info:{}", e.getCode(), e.getInfo());
        return Response.<String>builder()
                .code(e.getCode())
                .info(e.getInfo())
                .build();
    }

    /**
     * 处理所有未知的系统异常 (Exception)
     * 比如：NullPointerException, SQL Exception 等
     */
    @ExceptionHandler(Exception.class)
    public Response<String> handleException(Exception e) {
        log.error("系统异常捕获", e);
        // 生产环境通常隐藏具体堆栈，只返回通用错误码
        return Response.<String>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info(ResponseCode.UN_ERROR.getInfo())
                .build();
    }
}