package com.louis.user_center.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {
    //生成序列化
    private static final long serialVersionUID = 3562960082308203848L;
    //封装用户注册
    private String userAccount;
    private String userPassword;
    private String checkPassword;
    private String planetCode;

}
