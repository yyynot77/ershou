package com.campus.ershou.config;

import com.campus.ershou.interceptor.AuthInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

/**
 * Web 全局配置：CORS、静态资源、JWT 拦截白名单
 * <p>
 * 上传文件通过 /uploads/** 映射到 app.upload.path 本地目录（FileController 写入）
 * <p>
 * FIXME：/api/files/upload 在白名单，未登录也可上传，生产环境应加鉴权或限流
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private AuthInterceptor authInterceptor;
    @Value("${app.upload.path}")
    private String uploadPath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/health",
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/register/merchant",
                        "/api/auth/captcha",
                        "/api/products/search",
                        "/api/products/*",
                        "/api/categories",
                        "/api/banners",
                        "/api/shops/**",
                        "/api/files/upload"
                );
    }
}
