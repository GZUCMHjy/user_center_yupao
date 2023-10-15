package com.louis.user_center.model.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.Serializable;
import java.util.Date;

/**
 * @author louis
 * @version 1.0
 * @date 2023/10/2 11:49
 */
// 用户信息包装类 （脱敏）
@Data
public class UserVO implements Serializable {

    private static final long serialVersionUID = 1506999077657613043L;

    /**
     * 用户id
     */
    private Integer id;

    /**
     * 用户名
     */
    private String userName;


    /**
     * 邮箱
     */
    private String email;

    /**
     * 电话
     */
    private String phone;

    /**
     * 账号是否有效
     */
    private Integer isValid;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;


    /**
     * 用户账号
     */
    private String userAccount;

    /**
     *
     */
    private String avatarUrl;

    /**
     * 0--普通用户
     1--管理员
     */
    private Integer userRole;

    /**
     * 星球编号
     */
    private String planetCode;

    /**
     * 标签列表 json
     */
    private String tags;
}
