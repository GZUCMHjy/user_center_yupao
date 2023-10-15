package com.louis.user_center.controller;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.louis.user_center.common.BaseResponse;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.common.ResultUtils;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.request.UserLoginRequest;
import com.louis.user_center.model.request.UserRegisterRequest;
import com.louis.user_center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import static com.louis.user_center.constant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
@EnableScheduling // 主类开启定时任务
@Slf4j
//@CrossOrigin(origins = {"http://user.louisbrilliant.vip"},allowCredentials = "true")
public class UserController {
    @Resource
    private UserService userService;

    // 引入Redis 发挥缓存的作用
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
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
        //管理员访问 提取公有代码 单独封装成方法 到service去
        if (!userService.isAdmin(httpServletRequest)) {
            throw new BusinessException(ErrorCode.NULL_ERROR,"参数为空");
        }
        QueryWrapper<User> qw = new QueryWrapper<>();

        if (StringUtils.isNotBlank(username)) {
            qw.like("username", username);
        }
        List<User> userList = userService.list(qw);
        //这段代码不太懂 TODO
        //用户脱敏 敏感信息Password 设置为空(null)
        List<User> collect = userList.stream().map(
                user -> {
                    user.setUserPassword(null);
                    return userService.getSafetyUser(user);
                }).collect(Collectors.toList());
        return ResultUtils.success(collect);

    }
    @GetMapping("/recommend")
    //通过用户名模糊查询 返回用户列表
    public BaseResponse<Page<User>> recommendUsers(long pageSize,long pageNum,HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        // 如果有缓存 直接读缓存
        // 设计redisKey

        String redisKey = String.format("yupao:user:recommend:%s", loginUser.getId());
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User>  userPage = (Page<User>) valueOperations.get(redisKey);
        if(userPage != null){
            return ResultUtils.success(userPage);
        }
        // 定义新的查询对象
        // 包裹快捷键 ctrl+alt+t
        QueryWrapper<User> qw = new QueryWrapper<>();
        // 分页查询 海量数据
        userPage = userService.page(new Page<>(pageNum,pageSize),qw);
        // 写缓存
        // 发生异常 直接捕获 我们这次不需要交给 全局的异常处理器 进行处理异常
        try{
            valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);// 毫秒 过期缓存的过期时间
        }catch (Exception e){
            log.error("redis set key error",e);
        }
        return ResultUtils.success(userPage);
    }

    @PostMapping("/delete")
    //指定用户id删除用户
    public BaseResponse<Boolean> deleteUser(@RequestBody long id,HttpServletRequest httpServletRequest){
        //管理员操作 删除用户
        if (!userService.isAdmin(httpServletRequest)){

            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        if(id<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //mybatis_plus 逻辑删除 更新操作
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }
    @GetMapping("/search/tags")
//    传过来的参数不比填 也就是可以非空 默认为true 是必填 不能为空
    // 识别前端发的数据 通过@RequestParam 识别参数
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList){
        // 判断异常 controller层常常可以用来校验是否为空 service逻辑层 常常用来校验是否合法
        if(CollectionUtils.isEmpty(tagNameList)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> users = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(users);
    }

    /**
     * 修改用户信息
     * @param user 修改用户信息的user对象
     * @param request 后端获取当前前端发起请求的用户信息
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user,HttpServletRequest request){
        // 1. 传参是否为空
        if(user==null){
            throw  new BusinessException(ErrorCode.NULL_ERROR);
        }
        // getLoginUser 第一次进行检权
        User loginUser = userService.getLoginUser(request);
        // 表现层（外层） 再做一次检权
        if(loginUser==null){
            // 第二次进行检权
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        // 2. 校验权限 (单开在service进行逻辑的校验)
        // 3. 触发更新
        Integer result = userService.updateUser(user,loginUser);
        return ResultUtils.success(result);
    }


    /**
     * 匹配算法
     * @param num
     * @param request
     * @return
     */
    @GetMapping("/match")
    public BaseResponse<List<User>> listTeams(long num, HttpServletRequest request){
        if(num <= 0 || num >20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        return ResultUtils.success(userService.matchUsers(num,loginUser));

    }
}
