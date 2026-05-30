package com.campus.ershou.common;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusiness(BusinessException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> handleMethodValid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(this::fieldErrorText)
                .collect(Collectors.joining("；"));
        return Result.fail(msg.isEmpty() ? "参数校验失败" : msg);
    }

    @ExceptionHandler(BindException.class)
    public Result<?> handleBind(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(this::fieldErrorText)
                .collect(Collectors.joining("；"));
        return Result.fail(msg.isEmpty() ? "参数校验失败" : msg);
    }

    private String fieldErrorText(FieldError f) {
        String name = switch (f.getField()) {
            case "bankAccount" -> "银行账号";
            case "businessLicense" -> "营业执照";
            case "idCardImage" -> "身份证照片";
            case "shopName" -> "店铺名称";
            case "captchaCode" -> "验证码";
            default -> f.getField();
        };
        String tip = f.getDefaultMessage();
        if ("bankAccount".equals(f.getField())) {
            tip = "必须为16位数字";
        }
        return name + ": " + tip;
    }

    @ExceptionHandler(DataAccessException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleDb(DataAccessException e) {
        e.printStackTrace();
        return Result.fail("数据库访问失败，请确认 MySQL 已启动并已执行 sql/init.sql 初始化 2023011345 库");
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleOther(Exception e) {
        e.printStackTrace();
        return Result.fail("系统异常: " + e.getMessage());
    }
}
