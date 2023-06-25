package iss.ibf.pfm_expenses_server.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
// @EnableWebMvc 
// note: enabledwebmvc will disable default controller setting
public class SecurityConfig {

    // define a component based security configuration
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http builder configurations for authorize requests and form login (see below)
        // note: more specific requestMatchers need to be on top
        // http
        //     // .cors(cors -> cors.disable())
        //     .csrf(csrf -> csrf.disable())
        //     .authorizeHttpRequests(req -> req
        //                                     .requestMatchers("/api/account/register/**").permitAll()
        //                                     .requestMatchers("/api/account/login/**").permitAll()
        //                                     .requestMatchers("/api/account/logout/**").permitAll()
        //                                     .requestMatchers("/api/profile/**").permitAll()
        //                                     .requestMatchers("/api/transaction/**").permitAll()
        //                                     .requestMatchers("/api/googleapi/**").permitAll()
        //                                     .requestMatchers("/api/notification/**").permitAll())
        //     .formLogin(form -> form
        //                         .loginPage("http://localhost:4200")
        //                         .permitAll())
        //     .logout(logout -> logout
        //                         .logoutRequestMatcher(new AntPathRequestMatcher("http://localhost:4200"))
        //                         .permitAll());  
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(r -> r.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/favicon.ico").permitAll()
                                            .anyRequest().permitAll());                    

        return http.build();
    }
    
}
