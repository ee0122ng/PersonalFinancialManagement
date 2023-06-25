package iss.ibf.pfm_expenses_server.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import iss.ibf.pfm_expenses_server.config.CorsConfig;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public WebMvcConfigurer configureCors() {
        return new CorsConfig("/api/*", "*");
    }
    
}


