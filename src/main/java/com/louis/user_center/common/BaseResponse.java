package com.louis.user_center.common;

import lombok.Data;

import java.io.Serializable;
//运用泛型 提高通用性

/**
 * 通用公共通用响应类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    //信息码
    private int code;
    //泛型数据
    private T data;
    //请求数据的信息(成功/失败)
    private String message;
    //失败的详细描述
    private String description;
    //定义多个构造函数
    public BaseResponse(int code, T data, String message,String description) {
        this.code=code;
        this.data=data;
        this.message=message;
        this.description=description;
    }
    // 成功的统一返回结果
    public BaseResponse(int code, T data, String message) {

        this(code,data,message,"");
    }
    public BaseResponse(int code, T data) {
        this(code,data,"","");
    }
    // 失败的统一返回结果
    public BaseResponse(ErrorCode errorCode){

        this(errorCode.getCode(),null,errorCode.getMassage(),errorCode.getDescription());
    }
    public BaseResponse(ErrorCode errorCode,String message,String description){

        this(errorCode.getCode(),null,errorCode.getMassage(),errorCode.getDescription());
    }
}
