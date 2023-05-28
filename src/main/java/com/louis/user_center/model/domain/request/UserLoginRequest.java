package com.louis.user_center.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = 5898506683558100329L;

    private String userAccount;
    private String userPassword;

}
