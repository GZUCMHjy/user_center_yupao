package com.louis.user_center.service;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.louis.user_center.model.domain.User;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class UserServiceTest {
    @Resource
    UserService userService;
    @Test
    public void test_01(){

        User user = new User();
        user.setUserName("louis");
        user.setUserPassword("12345678");
        user.setEmail("");
        user.setPhone("12345678");
        user.setIsValid(0);
        user.setGender(0);
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        user.setIsDelete(0);
        user.setUserAccount("");
        user.setAvatarUrl("");
        boolean save = userService.save(user);
        System.out.println(user.getId());
        //断言常用于测试方法 业务方法常用于异常报错
        Assertions.assertTrue(save);
    }

    @Test
    void userRegister() {
//        System.out.println(userService.userRegister("louis", "12345678", "12345678"));
    }

//    @Test
//    void userLogin(){
//        System.out.println(userService.userLogin("louis", "12345678"));
//
//    }
    @Test
    void contextLoads() {
        // 前端传来tag列表
        List<String> tagsList = Arrays.asList("java","python");
        List<User> users = userService.searchUsersByTags(tagsList);
        Assert.assertNotNull(users);
    }

}