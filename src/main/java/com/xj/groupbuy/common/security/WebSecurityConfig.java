package com.xj.groupbuy.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xj.groupbuy.entity.User;
import com.xj.groupbuy.service.IUserService;
import com.xj.groupbuy.common.vo.CommonVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentRememberMeToken;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.session.ConcurrentSessionFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Author : zhangxiaojian
 * Date : 2021/3/11
 */

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    IUserService userService;
    @Autowired
    MyFilterInvocationSecurityMetadataSource myFilterInvocationSecurityMetadataSource;
    @Autowired
    MyUrlDecisionManager myUrlDecisionManager;

    @Autowired
    private DataSource dataSource;

    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
//        jdbcTokenRepository.setCreateTableOnStartup(true);
        return jdbcTokenRepository;
    }

    @Bean
    public RememberMeServices rememberMeServices() {
        JdbcTokenRepositoryImpl rememberMeTokenRepository = new JdbcTokenRepositoryImpl();
        rememberMeTokenRepository.setDataSource(dataSource);

        // ?????????????????? UserDetailsSerice ??????
        MyPersistentTokenBasedRememberMeServices rememberMeServices =
                new MyPersistentTokenBasedRememberMeServices("123", userService, rememberMeTokenRepository);
//        INTERNAL_SECRET_KEY
        rememberMeServices.setParameter("rememberMe");
        return rememberMeServices;
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/","/css/**", "/js/**", "/index.html", "/img/**", "/fonts/**","/static/**", "/favicon.ico", "/verifyCode","/register","/checkLogin");
    }

    @Bean
    LoginFilter loginFilter() throws Exception {
        LoginFilter loginFilter = new LoginFilter();

        // ?????????????????????????????????
        loginFilter.setAuthenticationSuccessHandler((request, response, authentication) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    User user = (User) authentication.getPrincipal();
                    user.setPassword(null);
                    CommonVO ok = CommonVO.ok("????????????!", user);
                    String s = new ObjectMapper().writeValueAsString(ok);
                    out.write(s);
                    out.flush();
                    out.close();
                }
        );
        // ????????????????????????????????????
        loginFilter.setAuthenticationFailureHandler((request, response, exception) -> {
                    response.setContentType("application/json;charset=utf-8");
                    PrintWriter out = response.getWriter();
                    CommonVO commonVO = CommonVO.error(exception.getMessage());
                    if (exception instanceof LockedException) {
                        commonVO.setMsg("????????????????????????????????????!");
                    } else if (exception instanceof CredentialsExpiredException) {
                        commonVO.setMsg("?????????????????????????????????!");
                    } else if (exception instanceof AccountExpiredException) {
                        commonVO.setMsg("?????????????????????????????????!");
                    } else if (exception instanceof DisabledException) {
                        commonVO.setMsg("????????????????????????????????????!");
                    } else if (exception instanceof BadCredentialsException) {
                        commonVO.setMsg("???????????????????????????????????????????????????!");
                    }
                    out.write(new ObjectMapper().writeValueAsString(commonVO));
                    out.flush();
                    out.close();
                }
        );
        loginFilter.setAuthenticationManager(authenticationManagerBean());
        loginFilter.setFilterProcessesUrl("/doLogin");
        ConcurrentSessionControlAuthenticationStrategy sessionStrategy = new ConcurrentSessionControlAuthenticationStrategy(sessionRegistry());
        sessionStrategy.setMaximumSessions(1);
        loginFilter.setSessionAuthenticationStrategy(sessionStrategy);
        
        loginFilter.setRememberMeServices(rememberMeServices());
        
        return loginFilter;
    }

    @Bean
    SessionRegistryImpl sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors();

        // ?????????????????????
        http.rememberMe().tokenRepository(persistentTokenRepository()).userDetailsService(userService).rememberMeParameter("rememberMe");
        
        http.authorizeRequests().withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
            @Override
            public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                object.setAccessDecisionManager(myUrlDecisionManager);
                object.setSecurityMetadataSource(myFilterInvocationSecurityMetadataSource);
                return object;
            }
        });

        http.logout().logoutSuccessHandler((req, resp, authentication) -> {
                    resp.setContentType("application/json;charset=utf-8");
                    PrintWriter out = resp.getWriter();
                    out.write(new ObjectMapper().writeValueAsString(CommonVO.ok("????????????!")));
                    out.flush();
                    out.close();
                }
        ).permitAll();

        http.csrf().disable().exceptionHandling().authenticationEntryPoint((req, resp, authException) -> {
                    // ????????????????????????????????????????????????
                    // ?????????????????????????????????????????????????????????
                    resp.setContentType("application/json;charset=utf-8");
                    resp.setStatus(401);
                    PrintWriter out = resp.getWriter();
                    CommonVO commonVO = CommonVO.error("????????????!");
                    if (authException instanceof InsufficientAuthenticationException) {
                        commonVO.setMsg("?????????????????????????????????!");
                    }
                    out.write(new ObjectMapper().writeValueAsString(commonVO));
                    out.flush();
                    out.close();
                }
        );

        http.addFilterAt(new ConcurrentSessionFilter(sessionRegistry(), event -> {
            HttpServletResponse resp = event.getResponse();
            resp.setContentType("application/json;charset=utf-8");
            resp.setStatus(401);
            PrintWriter out = resp.getWriter();
            out.write(new ObjectMapper().writeValueAsString(CommonVO.error("??????????????????????????????????????????????????????!")));
            out.flush();
            out.close();
        }), ConcurrentSessionFilter.class);

        http.addFilterAt(loginFilter(), UsernamePasswordAuthenticationFilter.class);

    }

    @Bean
    public HttpFirewall httpFirewall() {
        return new DefaultHttpFirewall();
    }
}
