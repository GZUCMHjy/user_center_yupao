package com.louis.user_center.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.mapper.UserMapper;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.louis.user_center.constant.UserConstant.USER_LOGIN_STATE;

/**
* @author 35064
* @description 针对表【user】的数据库操作Service实现
* @createDate 2023-03-26 01:57:16
*/
@Service
@Slf4j
//日志记录出错内容和时间 起到监控的作用 Lombok的注解
public class UserServiceImpl extends ServiceImpl<UserMapper, User>

    implements UserService {
    //可以不用注入Mapper类 Service就可以实现简单的增删改查~
    private static final String SALT="louis";//公共常量 以免后续使用出错~(快捷键prsf)
    @Resource
    UserMapper userMapper;

    /**
     * 用户注册校验逻辑
     * @param userAccount
     * @param userPassword
     * @param checkPassword
     * @param planetCode
     * @return
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {
        //检测用户账号、密码、和检验密码和星球编号非空
        if(StringUtils.isAnyEmpty(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //检验账号和密码长度
        if(userAccount.length()<4){
//            System.out.println("账号");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if(userPassword.length()<8){
//            System.out.println("密码");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        // 校验星球编号长度不超过五
        if(planetCode.length()>5){
//            System.out.println("星球编号");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号过长");
        }
        //正则表达式->特殊字符
        //过滤特殊字符
        String regex="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regex).matcher(userAccount);
        if(matcher.find()){
//            System.out.println("特殊字符");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名为空");
        }
        //密码和校验码
        if(!userPassword.equals(checkPassword)){
//            System.out.println("校验码");
            throw new BusinessException(ErrorCode.NULL_ERROR,"重复密码");
        }
        //账号不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count>0){
//            System.out.println("账号重复");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号重复");
        }
        //星球编号不能重复（查两次 一次查账号有无重复 另一次查星球编号有无重复）
        queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count>0){
//            System.out.println("编号重复");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"编号重复");
        }
        //密码加密 十六进制(查)

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //插入注册信息数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);//插入的是暗文的数据密码
        //补充的字段不要忘了插入 要不然数据看不到前端注册页面输入的星球编号!
        user.setPlanetCode(planetCode);
        boolean save = this.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"注册信息为空");
        }
        return user.getId();//装箱操作 大装小

    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //判断账号和密码是否为空
        //长度规范
        if(StringUtils.isAnyEmpty(userAccount,userPassword)){
             throw new BusinessException(ErrorCode.PARAMS_ERROR,"参数为空");
        }
        //检验账号和密码长度
        if(userAccount.length()<4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过短");
        }
        if(userPassword.length()<8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //正则表达式->特殊字符
        String regex="[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(regex).matcher(userAccount);
        if(matcher.find()){
//            return null;
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        //密码加密 十六进制(查)

        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询该账号或者密码匹配是否存在
        QueryWrapper<User> qw = new QueryWrapper<>();
        qw.eq("userAccount",userAccount);
        //换成encryptPassword
        qw.eq("userPassword",encryptPassword);

        User user = userMapper.selectOne(qw);
        //用户不存在
        if(user==null){
            log.info("user login failed,userAccount can't match userPassword");//出错的日志信息
//            System.out.println("无法找到该用户，该账号为空");
            //找不到就抛出异常
            throw new BusinessException(ErrorCode.NULL_ERROR,"无法找到该用户，该账号为空");
        }
        //用户信息脱敏
        User safetyUser = getSafetyUser(user);

        //后端设置session信息 存入浏览器当中 为后续前端发来带有cookie请求头
        request.getSession().setAttribute(USER_LOGIN_STATE,safetyUser);
        return safetyUser;
    }
    @Override
    public User getSafetyUser(User orginUser){
        //判空
        if(orginUser==null){
            return null;
        }

        //用户信息脱敏(密码)
        User saftyUser = new User();
        saftyUser.setId(orginUser.getId());
        saftyUser.setUserName(orginUser.getUserName());
        saftyUser.setEmail(orginUser.getEmail());
        saftyUser.setPhone(orginUser.getPhone());
        saftyUser.setIsValid(orginUser.getIsValid());
        saftyUser.setGender(orginUser.getGender());
        saftyUser.setCreateTime(new Date());
        saftyUser.setUpdateTime(new Date());
        saftyUser.setIsDelete(orginUser.getIsDelete());
        saftyUser.setUserAccount(orginUser.getUserAccount());
        saftyUser.setAvatarUrl(orginUser.getAvatarUrl());
        saftyUser.setUserRole(orginUser.getUserRole());
        saftyUser.setPlanetCode(orginUser.getPlanetCode());
        return saftyUser;
    }

    /**
     * 用户注销
     * @param request
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return  1;

    }

}




