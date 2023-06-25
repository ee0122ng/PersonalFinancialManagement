package iss.ibf.pfm_expenses_server.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public class CorsConfig implements WebMvcConfigurer {

    private String path;
    private String origin;

    public CorsConfig(String p, String o) {
        this.path = p;
        this.origin = o;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping(path)
            .allowedOrigins(origin);
    }
    
}
