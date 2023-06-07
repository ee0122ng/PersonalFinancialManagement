package iss.ibf.pfm_expenses_server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // define a component based security configuration
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http builder configurations for authorize requests and form login (see below)
        // note: more specific requestMatchers need to be on top
        http.csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(req -> req
                                            .requestMatchers("/api/register/**").permitAll()
                                            .requestMatchers("/api/login/**").permitAll()
                                            .requestMatchers("/api/complete/**").permitAll())
            // .formLogin(form -> form
            //                     .loginPage("/login")
            //                     .loginProcessingUrl("/login")
            //                     .defaultSuccessUrl("/home")
            //                     .permitAll())
            .logout(logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll());                       

        return http.build();
    }
    
}
