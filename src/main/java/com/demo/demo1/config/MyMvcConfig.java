package com.demo.demo1.config;

import com.demo.demo1.component.LoginHandlerInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//使用WebMvcConfigurerAdapter可以来扩展SpringMVC的功能
//@EnableWebMvc   不要接管SpringMVC
@Configuration
public class MyMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("search");
        registry.addViewController("/login.html").setViewName("login");
        registry.addViewController("/search").setViewName("search");
        registry.addViewController("/search.html").setViewName("search");
        registry.addViewController("/index").setViewName("search");
        registry.addViewController("/index.html").setViewName("search");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginHandlerInterceptor()).addPathPatterns("/**")
                .excludePathPatterns("/login.html","/login");
    }

    //    @Bean
//    public LocaleResolver localeResolver(){
//
////        return new MyLocaleResolver();
//    }

}
