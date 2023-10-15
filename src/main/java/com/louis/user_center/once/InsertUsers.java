package com.louis.user_center.once;
import java.util.Date;

import com.louis.user_center.mapper.UserMapper;
import com.louis.user_center.model.domain.User;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/25 13:22
 */
@Component
public class InsertUsers {
    @Resource
    private UserMapper userMapper;
//    设置时间无限长 就可以当做执行一次任务（单次任务）
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)
    public  void  doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM  = 1000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("");
            user.setUserPassword("");
            user.setEmail("");
            user.setPhone("");
            user.setGender(0);
            user.setUserAccount("");
            user.setAvatarUrl("https://img2.baidu.com/it/u=3449244732,113886072&fm=253&fmt=auto&app=138&f=JPEG?w=865&h=500");
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
