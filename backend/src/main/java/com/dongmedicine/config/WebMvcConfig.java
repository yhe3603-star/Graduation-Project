package com.dongmedicine.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload.base-path:public}")
    private String uploadBasePath;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        UrlPathHelper urlPathHelper = new UrlPathHelper();
        urlPathHelper.setUrlDecode(true);
        urlPathHelper.setDefaultEncoding("UTF-8");
        configurer.setUrlPathHelper(urlPathHelper);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path basePath = Paths.get(uploadBasePath);
        String absolutePath = basePath.isAbsolute() 
                ? uploadBasePath 
                : System.getProperty("user.dir") + "/" + uploadBasePath;
        
        absolutePath = absolutePath.replace("\\", "/");

        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + absolutePath + "/images/");
        
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:" + absolutePath + "/videos/");
        
        registry.addResourceHandler("/documents/**")
                .addResourceLocations("file:" + absolutePath + "/documents/");
        
        registry.addResourceHandler("/public/**")
                .addResourceLocations("file:" + absolutePath + "/");
    }
}
