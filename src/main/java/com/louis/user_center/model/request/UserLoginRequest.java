package com.louis.user_center.model.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5230131304593410772L;
    private String userAccount;
    private String userPassword;
}
