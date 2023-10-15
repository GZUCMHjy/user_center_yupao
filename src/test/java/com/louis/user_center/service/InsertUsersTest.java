package com.louis.user_center.service;

import com.louis.user_center.mapper.UserMapper;
import com.louis.user_center.model.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/25 13:38
 */
@SpringBootTest
public class InsertUsersTest {
    @Resource
    private UserMapper userMapper;


    @Resource
    private UserService userService;
    /**
     * 自定义 并发的线程数 数量 进行IO/CPU运算
     * corePoolSize 核心线程数 （下限）
     * maximumPoolSize 封顶的核心线程数（上限）
     * keepAliveTime 回收线程的时间（灭活时间）
     * ArrayBlockQueue 任务队列最多只能容纳的任务数
     */
    private ExecutorService executorService = new ThreadPoolExecutor
                   (60,
                    1000,
                    10000,
                    TimeUnit.MINUTES,
                    new ArrayBlockingQueue<>(10000));
    //    设置时间无限长 就可以当做执行一次任务（单次任务）
//    @Scheduled(initialDelay = 5000,fixedRate = Long.MAX_VALUE)

    /**
     * for循环插入数据
     */
    @Test
    public  void  doInsertUsers(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM  = 100000;
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("");
            user.setUserPassword("");
            user.setEmail("1346@qq.com");
            user.setPhone("13246");
            user.setGender(0);
            user.setUserAccount("假jy");
            user.setAvatarUrl("https://img2.baidu.com/it/u=3449244732,113886072&fm=253&fmt=auto&app=138&f=JPEG?w=865&h=500");
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 分批插入数据
     */
    @Test
    public  void  doInsertUsersByBetch(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM  = 100000;
        List<User> userList  = new ArrayList<>();
        for (int i = 0; i < INSERT_NUM; i++) {
            User user = new User();
            user.setUserName("");
            user.setUserPassword("");
            user.setEmail("1346@qq.com");
            user.setPhone("13246");
            user.setGender(0);
            user.setUserAccount("假jy");
            user.setAvatarUrl("https://img2.baidu.com/it/u=3449244732,113886072&fm=253&fmt=auto&app=138&f=JPEG?w=865&h=500");
            user.setUserRole(0);
            user.setPlanetCode("");
            user.setTags("[]");
            userList.add(user);
        }
        userService.saveBatch(userList,1000);
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 默认并发异步插入数据
     */
    @Test
    public  void  doInsertUsersByAsync(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM  = 100000;
        int j = 0 ;
        List<CompletableFuture<Void>> futureList  = new ArrayList<>();
        int batchSize = 5000;
        for (int i = 0; i < 20; i++) {
            List<User> userList  = new ArrayList<>();
            while(true){
                j++;
                User user = new User();
                user.setUserName("");
                user.setUserPassword("");
                user.setEmail("1346@qq.com");
                user.setPhone("13246");
                user.setGender(0);
                user.setUserAccount("假jy");
                user.setAvatarUrl("https://img2.baidu.com/it/u=3449244732,113886072&fm=253&fmt=auto&app=138&f=JPEG?w=865&h=500");
                user.setUserRole(0);
                user.setPlanetCode("");
                user.setTags("[]");
                userList.add(user);
                if(j % batchSize==0){
                    break;
                }
            }
            // 异步执行(在顺序无关紧要的情况下 是可以采用并发异步的)
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            });
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }

    /**
     * 自定义线程数量 并发插入数据
     */
    @Test
    public  void  doInsertUsersByDefineThread(){
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int INSERT_NUM  = 100000;
        int j = 0 ;
        List<CompletableFuture<Void>> futureList  = new ArrayList<>();
        int batchSize = 5000;
        for (int i = 0; i < 20; i++) {
            List<User> userList  = new ArrayList<>();
            while(true){
                j++;
                User user = new User();
                user.setUserName("");
                user.setUserPassword("");
                user.setEmail("1346@qq.com");
                user.setPhone("13246");
                user.setGender(0);
                user.setUserAccount("假jy");
                user.setAvatarUrl("https://img2.baidu.com/it/u=3449244732,113886072&fm=253&fmt=auto&app=138&f=JPEG?w=865&h=500");
                user.setUserRole(0);
                user.setPlanetCode("");
                user.setTags("[]");
                userList.add(user);
                if(j % batchSize==0){
                    break;
                }
            }
            // 异步执行(在顺序无关紧要的情况下 是可以采用并发异步的)
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                System.out.println(Thread.currentThread().getName());
                userService.saveBatch(userList, batchSize);
            },executorService);// 自定义线程数数量 进行并发操作
            futureList.add(future);
        }

        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());
    }
}
