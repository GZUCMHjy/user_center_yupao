package com.louis.user_center.model.request;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 16:37
 */
@Data
public class TeamUpdateRequest implements Serializable {

    private static final long serialVersionUID = -7565177016857601279L;
    /**
     * 更新id
     */

    private Long id;

    /**
     * 队伍名称
     */

    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 过期时间
     */
    private Date expireTime;


    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     *
     */
    private Date updateTime;

    /**
     * 密码
     */
    private String password;

}
