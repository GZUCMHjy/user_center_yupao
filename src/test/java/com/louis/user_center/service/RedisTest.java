package com.louis.user_center.service;

import com.louis.user_center.model.domain.User;

import org.junit.jupiter.api.Assertions;
// 测试包 得用 Jupiter 不能是 junit
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/25 22:17
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;


    @Test
    public void test(){
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("louisString","粥粥");
        valueOperations.set("louisDouble",1.0);
        valueOperations.set("louisInt",1);
        User user = new User();
        user.setId(1);
        user.setUserName("louis");
        valueOperations.set("louisUser",user);
        // 查
        Object louis = valueOperations.get("louisString");
        Assertions.assertTrue("粥粥".equals((String) louis));
        louis = valueOperations.get("louisInt");
        Assertions.assertTrue(1==(Integer) louis);
        louis = valueOperations.get("louisDouble");
        Assertions.assertTrue(1.0 == (double) louis );
        System.out.println(valueOperations.get("louisUser"));
        // 删除
        redisTemplate.delete("louisString");
    }
}
