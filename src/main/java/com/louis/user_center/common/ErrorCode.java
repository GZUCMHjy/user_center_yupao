package com.louis.user_center.common;

public enum ErrorCode {
    //多个错误枚举值
    //多个枚举值之间需要有逗号相隔
    PARAMS_ERROR(40000,"请求参数错误",""),
    NULL_ERROR(40001,"请求数据为空",""),
    NOT_LOGIN(40100,"未登录",""),
    FORBIDDEN(40300,"禁止操作",""),
    NOT_AUTH(40101,"无权限",""),
    SYSTEM_ERROR(50000,"系统报错","");

    private final int code;
    private final String massage;
    private final String description;

    ErrorCode(int code, String massage, String description) {
        this.code = code;
        this.massage = massage;
        this.description = description;
    }
    //枚举类不能有set方法 因为属性构成了 public static final
    public int getCode() {
        return code;
    }

    public String getMassage() {
        return massage;
    }

    public String getDescription() {
        return description;
    }
}
