package com.louis.user_center.common;


public class ResultUtils {
    //成功 只有一个原因
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }
    //失败 有多个原因
    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }
    //失败 有多个原因
    public static BaseResponse error(ErrorCode errorCode,String message,String description){
        return new BaseResponse(errorCode.getCode(),null,message,description);
    }
    public static BaseResponse error(ErrorCode errorCode,String description){
        return new BaseResponse(errorCode.getCode(),errorCode.getMassage(),description);
    }
    public static BaseResponse error(int code,String message,String description){
        return new BaseResponse(code,null,message,description);
    }
}
