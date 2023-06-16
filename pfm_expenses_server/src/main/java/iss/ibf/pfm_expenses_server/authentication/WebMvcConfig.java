package iss.ibf.pfm_expenses_server.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import iss.ibf.pfm_expenses_server.config.CorsConfig;

@EnableWebMvc
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/{path:^(?!api$).*$}").setViewName("forward:/");
    }

    @Bean
    public WebMvcConfigurer configureCors() {
        return new CorsConfig("/*", "*");
    }
    
}
