package br.com.course.auth.security.config;

import br.com.course.auth.security.filter.JWTUserAndPasswordAuthFilter;
import br.com.course.core.property.JWTConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import javax.servlet.http.HttpServletResponse;

@EnableWebSecurity
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SecurityCredentialsConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;

    private final JWTConfiguration jwtConfiguration;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .cors().configurationSource(httpServletRequest -> new CorsConfiguration().applyPermitDefaultValues())
                .and()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .exceptionHandling()
                    .authenticationEntryPoint((httpServletRequest, httpServletResponse, e)
                                -> httpServletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                .and()
                    .addFilter(new JWTUserAndPasswordAuthFilter(authenticationManager(),jwtConfiguration))
                        .authorizeRequests()
                            .antMatchers(jwtConfiguration.getLoginUrl()).permitAll()
                            .antMatchers("course/admin/**").hasRole("ADMIN")
                                .anyRequest().authenticated();
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }
}

