package com.louis.user_center.model.request;



import lombok.Data;

import java.io.Serializable;


/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 10:14
 */
@Data
public class TeamJoinRequest implements Serializable {


    private static final long serialVersionUID = -6931945639901269520L;
    /**
     * 队伍id
     */

    private Long teamId;


    /**
     * 密码
     */
    private String password;


}
