package com.louis.user_center.service.impl;
import java.util.*;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.louis.user_center.common.ErrorCode;
import com.louis.user_center.exception.BusinessException;
import com.louis.user_center.mapper.UserMapper;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.model.vo.UserVO;
import com.louis.user_center.service.UserService;
import com.louis.user_center.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.description.method.MethodDescription;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Generated;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.louis.user_center.constant.UserConstant.ADMIN_STATE;
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名为空");
        }
        //密码和校验码
        if(!userPassword.equals(checkPassword)){
//            System.out.println("校验码");
            throw new BusinessException(ErrorCode.NULL_ERROR,"两次输入密码不同");
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
        // 插入数据
        userMapper.insert(user);
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
        saftyUser.setTags(orginUser.getTags());
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

    /**
     * 通过标签搜索用户
     * @param tagNameList
     * @return
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList){
            // 判空
            if(CollectionUtils.isEmpty(tagNameList)){
                throw new BusinessException(ErrorCode.PARAMS_ERROR);
            }
            // 1. 采用sql查询数据
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for(String tagName : tagNameList){
                // 遍历获取连接查询sql语句（模糊匹配）
              queryWrapper=  queryWrapper.like("tags",tagName);
        }
        // 简化形式 lambada表达式
        // 同时化成List
        List<User> userList = userMapper.selectList(queryWrapper);
                userList.forEach(user->{
                    getSafetyUser(user);
                });

    // 2. 内存中查询 （灵活）
            //QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            //// 2.1 先查询所有用户
            //List<User> userList = userMapper.selectList(queryWrapper);
            //// 2.2 在内存中查询用户标签
            //Gson gson = new Gson();
            //// 快捷键iter
            //// 比单纯的for循环遍历更快！
            //return userList.stream().filter((user)->{
            //    String  tagStr = user.getTags();
            //    if(StringUtils.isBlank(tagStr)){
            //        return false;
            //    }
            //    // java装json对象 应使用序列化库 去maven repository 下载对应的依赖
            //    Set<String> tempTagNameSet = gson.fromJson(tagStr, new TypeToken<Set<String>>() {
            //    }.getType());
            //  任何数据结构都需要进行判空 否则空指针异常
            // tempTagNameSet = Optional.ofNullable(tempTagNameSet).orElse(new HashSet<>());
            //    for (String tagName : tagNameList) {
            //        if(!tempTagNameSet.contains(tagName)){
            //            return false;
            //        }
            //    }return true;
            //}).map((this::getSafetyUser)).collect(Collectors.toList());
        return userList.stream().map((this::getSafetyUser)).collect(Collectors.toList());

    }

    /**
     * 修改用户信息
     * @param user 是前端传来要更新的user对象
     * @param loginUser 当前用户账户user信息
     * @return
     */
    @Override
    public int updateUser(User user,User loginUser) {
        int userId = user.getId();
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 补充校验 如果用户没有任何要更新的值，就直接报错，不用再执行update语句进行更新
        // 仅管理员和本人进行修改用户信息
        // 1. 管理员修改所有用户信息
//        if(isAdmin(loginUser)){
//            User oldUser = userMapper.selectById(userId);
//            if(oldUser == null){
//                throw new BusinessException(ErrorCode.NULL_ERROR);
//            }
//           return  userMapper.updateById(user);
//        }
////             2. 当前用户可以修改自己账户的信息
//        if(userId!=loginUser.getId()){
//            // 不是自己用户 不能修改 报异常
//            throw new BusinessException(ErrorCode.NOT_AUTH);
//        }
        if(!isAdmin(loginUser) && user.getId()!=loginUser.getId()){
            throw new BusinessException(ErrorCode.NOT_AUTH);
        }
        // 是对自己当前的用户信息进行修改 放行
        User oldUser = userMapper.selectById(userId);
        if(oldUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return  userMapper.updateById(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if(request==null){
            return null;
        }
        // 从request提取session 进而获取当中的用户信息
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        if(userObj==null){
            throw  new BusinessException(ErrorCode.NOT_AUTH);
        }
        return (User) userObj ;
    }

    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //通过session获取该账号
        //然后判断该账号是否为管理员
        Object attribute = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user=(User) attribute;
        return user!=null && user.getUserRole().intValue()==ADMIN_STATE.intValue();
    }

    @Override
    public boolean isAdmin(User loginUser) {
        return  loginUser.getUserRole().toString().equals(ADMIN_STATE.toString());
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id","tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags,new TypeToken<List<String>>(){
        }.getType());
        // 用户列表
        List<Pair<User,Long>> list = new ArrayList<>();


        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签 或者是自己 就不推荐了
            if(StringUtils.isBlank(userTags) || user.getId() == loginUser.getId()){
                continue;
            }
            List<String> userTagsList = gson.fromJson(userTags,new TypeToken<List<String>>(){
            }.getType());
            //计算分数
            long distance = AlgorithmUtils.minDistance(tagList,userTagsList);
            list.add(new Pair<>(user,distance));
        }
        List<Pair<User,Long>> topUserList = list.stream()
                .sorted((a,b)-> (int) (a.getValue()-b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
        List<Integer> userIdList = topUserList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id",userIdList);
        // 1,3,2
        // User1  User2 User3
        // 1 = > User1 2 => User2 3 =>User3
        Map<Integer,List<User>> userIdUserListMap= this.list(userQueryWrapper)
                .stream()// 提高性能 使用流（Stream）来实现
                .map(user -> getSafetyUser(user)) // 用户进行脱敏 通过map进行映射来完成
                .collect(Collectors.groupingBy(User::getId)); // 用户分组 根据UserId来确定
        List<User> finalUserList = new ArrayList<>();
        for (Integer userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }
}




