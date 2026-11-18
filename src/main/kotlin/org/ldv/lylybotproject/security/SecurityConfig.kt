package org.ldv.lylybotproject.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import java.util.UUID

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(http: HttpSecurity, userDetailsService: UserDetailsService): SecurityFilterChain {
        http
            .authorizeHttpRequests {
                it.requestMatchers("/", "/login", "/css/**", "/js/**", "/img/**", "/vendor/**", "/fonts/**", "/error").permitAll()
                    .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                    .anyRequest().authenticated()
            }
            .formLogin {
                it.loginPage("/login")
                    .usernameParameter("email")
                    .passwordParameter("motDePasse")
                    .defaultSuccessUrl("/chat", true)
                    .failureUrl("/login?erreur")
            }
            .rememberMe {
                it.rememberMeParameter("remember")
                    .key(UUID.randomUUID().toString())
                    .userDetailsService(userDetailsService)
            }
            .logout { it.logoutUrl("/logout").logoutSuccessUrl("/login?deconnecte") }
        return http.build()
    }
}
