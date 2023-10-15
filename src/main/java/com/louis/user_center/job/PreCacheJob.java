package com.louis.user_center.job;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/26 18:32
 */

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.louis.user_center.model.domain.User;
import com.louis.user_center.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient; // redis的下沿工具 redisson 操作分布式锁
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 预热缓存
 */
@Component
@Slf4j
public class PreCacheJob {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private UserService userService;

    @Resource
    private RedissonClient redissonClient;

    // 重点用户 类似于白名单
    // 最好要动态查询这样比较好 现在是写死了
    // TODO
    private List<Integer> mainUserList = Arrays.asList(2);


    // Spring 自带的定时任务注解
    // 大概在一个小时候 自动更新缓存（相当于预热缓存数据，实现时时秒开）
    @Scheduled(cron = "0 12 1 * * *")
    public void doCacheUserRecommendUser(){
        // 获取实例锁
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try{
            // 只能有一个线程 抢到锁
            if(lock.tryLock(0,30000L,TimeUnit.MILLISECONDS)){
                System.out.println("getLock:"+Thread.currentThread().getId());
                for (Integer userId : mainUserList){
                    QueryWrapper<User> qw = new QueryWrapper<>();
                    // 分页查询 海量数据
                    Page<User> userPage = userService.page(new Page<>(1,20),qw);
                    String redisKey = String.format("yupao:user:recommend:%s", mainUserList);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 写缓存
                    try{
                        // 毫秒 确定过期缓存的过期时间（一定要有过期时间）
                        valueOperations.set(redisKey,userPage,30000, TimeUnit.MILLISECONDS);
                    }catch (Exception e){
                        log.error("redis set key error",e);
                    }
                }
            }
        }catch(InterruptedException e){
            e.printStackTrace();
        }finally {
            // 自己释放锁（必须执行）
            if(lock.isHeldByCurrentThread()){ //该方法说明 当前线程执行完任务时为true 则放行if 执行放锁操作
                System.out.println("unLock:"+ Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }
}
