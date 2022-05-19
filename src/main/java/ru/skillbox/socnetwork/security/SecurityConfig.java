package ru.skillbox.socnetwork.security;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import ru.skillbox.socnetwork.exception.CustomAuthenticationEntryPoint;

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Getter
    @Value("${skillbox.app.server}")
    private String server;
    @Getter
    @Value("${skillbox.app.localhost}")
    private String localhost;

    private final JwtCsrfFilter jwtCsrfFilter;
//    private final List<String> hosts = List.of("http://localhost:8080", "http://localhost:8086", "http://localhost",
//            "http://195.133.201.227:8080", "http://195.133.201.227:8086", "http://195.133.201.227");

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                .configurationSource(corsConfigurationSource())
                .and()
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/**", "/api/v1/account/register").permitAll()
                .antMatchers("/static/**", "/api/v1/platform/**", "/*").permitAll()
                .antMatchers("/api/v1/account/password/**").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint())
                .and()
                .addFilterBefore(jwtCsrfFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.applyPermitDefaultValues();
        configuration.setAllowedOrigins(getHosts());
        configuration.setAllowedMethods(List.of("OPTIONS", "DELETE", "POST", "GET", "PATCH", "PUT"));
        configuration.setExposedHeaders(List.of("Content-Type", "X-Requested-With", "accept", "Origin",
                "Access-Control-Request-Method", "Access-Control-Request-Headers", "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private List<String> getHosts() {
        List<String> links = new ArrayList<>();
        String http = "http://";
        String[] hosts = new String[]{localhost, server};
        String[] ports = new String[]{":8080", ":8086", ""};
        for (String host : hosts) {
            for (String port : ports) {
                links.add(http + host + port);
            }
        }
        return links;
    }
}
