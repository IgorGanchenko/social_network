package ru.skillbox.socnetwork.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final JwtCsrfFilter jwtCsrfFilter;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .httpBasic().disable()
                .csrf().disable()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                .antMatchers("/api/v1/auth/**", "/api/v1/account/register").permitAll()
                .antMatchers("/static/**", "/api/v1/platform/**").permitAll()
                .antMatchers("/*", "/api/v1/auth/logout").permitAll()
                .anyRequest()
                .authenticated()
                .and()
                .addFilterBefore(jwtCsrfFilter, UsernamePasswordAuthenticationFilter.class);
    }

    /**
     * TODO check PasswordEncoder
     * убрал из BCryptPasswordEncoder() силу кодировки (12), чтобы с бд проблем не было.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
