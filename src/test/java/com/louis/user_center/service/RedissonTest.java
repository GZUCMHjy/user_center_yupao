package com.louis.user_center.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.louis.user_center.model.domain.User;
import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/26 22:17
 */
@SpringBootTest
public class RedissonTest {

    @Resource
    RedissonClient redissonClient;


    @Test
    public void test(){
        // 数据存入到JVM 里面当中
        List<String> list = new ArrayList<>();
        list.add("louis");
        System.out.println("list:"+ list.get(0));
//        list.remove(0);


        // 数据存入到redis内存中
        RList<String> rlist = redissonClient.getList("test_louis_list");

        rlist.add("louis");
        System.out.println("rlist:"+ rlist.get(0));
//        rlist.remove(0);
    }
    @Test
    public void watchDog(){
        // 获取实例锁
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");

        try{
            // 只能有一个线程 抢到锁
            // -1 redis自带重置功能 —— 看门狗机制
            if(lock.tryLock(0,-1, TimeUnit.MILLISECONDS)){
                // 睡到30秒（过期时间） 倒计时20s后 又重置到30s —— 相当于看门口狗 每10s续期一次
                // doSomeThing();
                Thread.sleep(300000);
                System.out.println("getLock:"+Thread.currentThread().getId());
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
