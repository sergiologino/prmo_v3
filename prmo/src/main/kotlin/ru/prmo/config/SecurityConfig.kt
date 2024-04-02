package ru.prmo.config

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.invoke
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.csrf.CookieCsrfTokenRepository
import ru.prmo.service.UserService

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val userService: UserService,
    private val passwordEncoder: PasswordEncoder,

) {
    @Autowired
    lateinit var  urlAuthenticationSuccessHandler: UrlAuthenticationSuccessHandler

    @Bean
    fun filterChain(http: HttpSecurity, ): SecurityFilterChain {
        http {
            csrf {
                disable()  //сделать кастомный логаут
            }

            authorizeHttpRequests {
                authorize("test/unsecured/**", permitAll)
                authorize("/styles/**", permitAll)
                authorize("/images/**", permitAll)
                authorize("/js/**", permitAll)
//                authorize("/logout", permitAll)
                authorize("test/secured", authenticated)
                authorize("/user/**", authenticated)
                authorize("/admin/**", hasRole("ADMIN"))

            }

            formLogin {
                loginPage = "/login"
                authenticationSuccessHandler = urlAuthenticationSuccessHandler
                permitAll()
            }
            logout {

                logoutSuccessUrl = "/login"
                permitAll()

            }
            rememberMe {  }
            httpBasic { }
        }
        return http.build()
    }

    @Bean
    fun authenticationManager(): AuthenticationManager {
        val authenticationProvider = DaoAuthenticationProvider()
        authenticationProvider.setPasswordEncoder(passwordEncoder)
        authenticationProvider.setUserDetailsService(userService)
        return ProviderManager(authenticationProvider)
    }
}