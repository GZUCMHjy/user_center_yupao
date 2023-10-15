package com.louis.user_center.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author louis
 * @version 1.0
 * @date 2023/6/17 20:35
 */

/**
 * 设置全局web拦截器
 * 解决跨域问题
 * 实现WebMvcConfigurer接口（适用于Spring Boot）
 */
@Configuration
public class CorsMapping implements WebMvcConfigurer {
    @Override
    /**
     * 重新跨域支持方法
     * CorsRegistry  开启跨域注册
     */
    public void addCorsMappings(CorsRegistry registry) {
        //addMapping 添加可跨域的请求地址
        registry.addMapping("/**")
                //设置跨域 域名权限 规定由某一个指定的域名+端口能访问跨域项目
                .allowedOrigins("http://127.0.0.1:5173", "http://user.louisbrilliant.vip", "http://42.193.201.114")
//                .allowedOriginPatterns("*")
                //是否开启cookie跨域
                .allowCredentials(true)
                //规定能够跨域访问的方法类型
                .allowedMethods("GET","POST","DELETE","PUT","OPTIONS")
                //添加验证头信息  token
                .allowedHeaders("*")
                .exposedHeaders("*");
                //预检请求存活时间 在此期间不再次发送预检请求
//                .maxAge(3600);
    }
//    @Override
//    public  void  addCorsMappings(CorsRegistry registry){
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:8080")
//                .allowedHeaders("*")
//                .allowedMethods("*")
//                .maxAge(30*1000);
//    }
}
