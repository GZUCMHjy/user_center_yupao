//package com.louis.user_center.config;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


//
////import org.springframework.context.annotation.Configuration;
////import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
////import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
////
/////**
//// * @author Andon
//// * 2021/12/29
//// */
////@Configuration
////public class WebMvcConfigurer extends WebMvcConfigurationSupport {
////
////    /**
////     * 发现如果继承了WebMvcConfigurationSupport，则在yml中配置的相关内容会失效。 需要重新指定静态资源
////     */
////    @Override
////    public void addResourceHandlers(ResourceHandlerRegistry registry) {
////        registry.addResourceHandler("/**").addResourceLocations(
////                "classpath:/static/");
////        registry.addResourceHandler("swagger-ui.html", "doc.html").addResourceLocations(
////                "classpath:/META-INF/resources/");
////        registry.addResourceHandler("/webjars/**").addResourceLocations(
////                "classpath:/META-INF/resources/webjars/");
////        super.addResourceHandlers(registry);
////    }
////
////}
//
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//public class WebMvcConfig implements WebMvcConfigurer {
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        registry.addResourceHandler("/**").addResourceLocations("classpath:/static/");
//        registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//}
// tag::swagger-ui-configurer[]
//public class SwaggerUiWebMvcConfigurer implements WebMvcConfigurer {
//    private final String baseUrl;
//
//    public SwaggerUiWebMvcConfigurer(String baseUrl) {
//        this.baseUrl = baseUrl;
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        String baseUrl = StringUtils.trimTrailingCharacter(this.baseUrl, '/');
//        registry.
//                addResourceHandler(baseUrl + "/swagger-ui/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
//                .resourceChain(false);
//    }
//
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController(baseUrl + "/swagger-ui/")
//                .setViewName("forward:" + baseUrl + "/swagger-ui/index.html");
//    }
//}

