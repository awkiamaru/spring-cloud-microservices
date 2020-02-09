package br.com.course.auth.security.config;

import br.com.course.auth.security.filter.JWTUserAndPasswordAuthFilter;
import br.com.course.config.SecurityTokenConfig;
import br.com.course.core.property.JWTConfiguration;
import br.com.course.token.creator.TokenCreator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@EnableWebSecurity
public class SecurityCredentialsConfig extends SecurityTokenConfig {
    private final UserDetailsService userDetailsService;
    private final TokenCreator tokenCreator;

    public SecurityCredentialsConfig(
                                     JWTConfiguration jwtConfiguration,
                                     @Qualifier("userDetailServiceImpl") UserDetailsService userDetailsService,
                                     TokenCreator tokenCreator) {
        super(jwtConfiguration);
        this.userDetailsService = userDetailsService;
        this.tokenCreator = tokenCreator;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .addFilter(new JWTUserAndPasswordAuthFilter(authenticationManager(), jwtConfiguration, tokenCreator));
        super.configure(http);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
