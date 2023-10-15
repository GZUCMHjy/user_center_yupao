package com.louis.user_center.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.louis.user_center.model.domain.Tag;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author 35064
* @description 针对表【user】的数据库操作Service
* @createDate 2023-03-26 01:57:16
*/
//已有基本的CRUD的方法
// 写在service接口的方法 通常是因为要被复用（调用）
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

    /**
     * 通过用户标签搜索用户
     * @param tagNameList
     */
    List<User>  searchUsersByTags(List<String > tagNameList);

    /**
     * 修改用户信息
     * @param user
     * @return
     */
    int updateUser(User user,User loginUser);

    /**
     * 获取登录用户的信息
     * @param request
     * @return
     */
     User getLoginUser(HttpServletRequest request);

    /**
     * 是否为管理员
     * @param httpServletRequest
     * @return
     */
     boolean isAdmin(HttpServletRequest httpServletRequest);

    /**
     * 重载isAdmin方法
     * @param loginUser
     * @return
     */
     boolean isAdmin(User loginUser);

    /**
     * 匹配用户
     * @param num
     * @param loginUser
     * @return
     */
    List<User> matchUsers(long num, User loginUser);
}
