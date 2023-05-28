package com.louis.user_center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.louis.user_center.model.domain.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author 35064
* @description 针对表【user】的数据库操作Service
* @createDate 2023-03-26 01:57:16
*/
//已有基本的CRUD的方法
public interface UserService extends IService<User> {

    /**
     * 用户注册账号
     * @param account
     * @param passWord
     * @param checkPassWord
     * @param planetCode
     * @return
     */
    public long userRegister(String account,String passWord,String checkPassWord,String planetCode);

    /**
     * 用户登录账号
     * @param userAccount
     * @param password
     * @param request
     * @return
     */
    public User userLogin(String userAccount, String password, HttpServletRequest request);

    /**
     * 用户数据脱敏
     * @param orginUser
     * @return
     */
    public User getSafetyUser(User orginUser);

    /**
     * 用户注销账号(退出登录)
     * @param request
     * @return
     */
    public int userLogout(HttpServletRequest request);

}
