package com.louis.user_center.config;


import lombok.Data;
import org.redisson.Redisson;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.redisson.api.RedissonClient;

/**
 * @author louis
 * @version 1.0
 * @date 2023/9/26 22:05
 */
@Configuration
@ConfigurationProperties(prefix = "spring.redis") // 读取配置文件参数的注解 就不用额外在参数上面加上@Value
@Data
public class RedissonConfig {

    private String port;

    private String host;

    /**
     * 实例并初始化Redisson
     * @return
     */
    @Bean
    public RedissonClient redissonClient(){
        // 1. 创建配置
        Config config = new Config();
        // 字符串格式化
        String redisAddress =String.format("redis://%s:%s",host,port);
        config.useSingleServer().setAddress(redisAddress).setDatabase(3);
        // 2. 创建redis实例
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
