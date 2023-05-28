package com.louis.user_center.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.louis.user_center.common.BaseResponse;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.common.ResultUtils;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.domain.request.UserLoginRequest;
import com.louis.user_center.model.domain.request.UserRegisterRequest;
import com.louis.user_center.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.louis.user_center.constant.UserConstant.ADMIN_STATE;
import static com.louis.user_center.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {


    @Resource
    UserService userService;
    @PostMapping("/register")
    // 返回类型是一个通用公共响应的泛型类
    public BaseResponse userRegister(@RequestBody UserRegisterRequest userRegisterRequest){
        if(userRegisterRequest==null){
//            System.out.println("userRegisterRequest为空");
            throw new BusinessException(ErrorCode.NULL_ERROR,"注册失败");
        }
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        //任意一方为空 则为true
        if(StringUtils.isAnyEmpty(userAccount,userPassword,checkPassword,planetCode)){
//            System.out.println("参数为空");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }//返回用户注册情况
        long res = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);

        return ResultUtils.success(res);
    }
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest httpServletRequest){
        if(userLoginRequest==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        //其中一个为空 则抛出异常
        if(StringUtils.isAnyEmpty(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, httpServletRequest);
        return ResultUtils.success(user);
    }
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(@RequestBody  HttpServletRequest request){
        if(request ==null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        else{
            int i = userService.userLogout(request);
            return ResultUtils.success(i);
        }
    }
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request){
        //通过获取登录过的用户的session 进而获取用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser =(User)userObj;
        //判空！！！
        if(currentUser==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN,"用户未登录");
        }
        Integer id = currentUser.getId();
        //查库找出用户信息
        User user= userService.getById(id);
        User safetyUser = userService.getSafetyUser(user);

        return ResultUtils.success(safetyUser);
    }
    @GetMapping("/search")
    //通过用户名模糊查询 返回用户列表
    public BaseResponse<List<User>> searchUsers(String username,HttpServletRequest httpServletRequest) {
        //鉴权
        //管理员访问 提取公有代码 单独封装成方法
        if (!isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"参数为空");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            qw.like("username", username);
        }
        List<User> userList = userService.list(qw);
        //这段代码不太懂
        //用户脱敏 敏感信息Password 设置为空(null)
        List<User> collect = userList.stream().map(
                user -> {
                    user.setUserPassword(null);
                    return userService.getSafetyUser(user);
                }).collect(Collectors.toList());
        return ResultUtils.success(collect);

    }

    @PostMapping("/delete")
    //指定用户id删除用户
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest httpServletRequest){
        //管理员操作 删除用户
        if (!isAdmin(httpServletRequest)){

            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //mybatis_plus 逻辑删除 更新操作
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }
    //是否为管理员
    private boolean isAdmin(HttpServletRequest httpServletRequest){
        //通过session获取该账号
        //然后判断该账号是否为管理员
        Object attribute = httpServletRequest.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) attribute;
        return user!=null && user.getUserRole() == ADMIN_STATE;
    }

}
