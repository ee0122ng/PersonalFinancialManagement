package iss.ibf.pfm_expenses_server.authentication;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PUT", "DELETE"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // define a component based security configuration
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http builder configurations for authorize requests and form login (see below)
        // note: more specific requestMatchers need to be on top
        http
            // .cors(cors -> cors.disable())
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(req -> req
                                            .requestMatchers("http://localhost:4200").permitAll()
                                            .requestMatchers("/api/account/register/**").permitAll()
                                            .requestMatchers("/api/account/login/**").permitAll()
                                            .requestMatchers("/api/account/logout/**").permitAll()
                                            .requestMatchers("/api/profile/**").permitAll()
                                            .requestMatchers("/api/transaction/**").permitAll()
                                            .requestMatchers("/api/googleapi/**").permitAll()
                                            .requestMatchers("/api/notification/**").permitAll())
            .formLogin(form -> form
                                .loginPage("http://localhost:4200")
                                .permitAll())
            .logout(logout -> logout
                                // .logoutRequestMatcher(new AntPathRequestMatcher("http://localhost:4200"))
                                .permitAll());                      

        return http.build();
    }
    
}
