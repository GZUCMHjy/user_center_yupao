package com.louis.user_center.common;

/**
 * 工具类
 * 方法均为静态
 */
public class ResultUtils {

    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0,data,"ok");
    }

    /**
     * 返回类型前面加上尖括号
     * 表明返回类型泛型
     * 前后的泛型T都要带上 否则编译器无法识别T是泛型
     * @param errorCode
     * @return
     * @param <T>
     */
    public static <T> BaseResponse<T> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String message,String description){
        return new BaseResponse<>(errorCode.getCode(),null,message,description);
    }
    public static <T> BaseResponse<T> error(ErrorCode errorCode,String description){
        return new BaseResponse<>(errorCode.getCode(),description);
    }
    public static <T> BaseResponse<T> error(int code,String message,String description){
        return new BaseResponse<>(code,null,message,description);
    }
}
