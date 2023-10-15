package com.louis.user_center.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 11:49
 */
// 维护用户和队伍的关系表（TeamUserVO）的封装类
@Data
public class TeamUserVO implements Serializable {

    private static final long serialVersionUID = -8964222493355458560L;
    /**
     * id
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
     * 最多人数
     */
    private Integer maxNum;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 0 - 公开，1 - 私有，2 - 加密
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人用户信息
     */

    private UserVO  createUser;

    /**
     * 是否加入队伍
     */
    private Boolean hasJoin;

    /**
     * 队伍中的加入人数
     */
    private Integer hasJoinNum;

}
