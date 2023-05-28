package com.louis.user_center.constant;
//常量定义 写接口属性 默认是public static final
public interface UserConstant {
    String USER_LOGIN_STATE="userLoginStatus";
    /**
     * 管理员
     */
    Integer ADMIN_STATE=1;
    /**
     * 普通用户
     */
    Integer DEFAULT_STATE=0;
}
