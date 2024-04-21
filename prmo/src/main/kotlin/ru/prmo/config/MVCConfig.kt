package ru.prmo.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebMvc
class MVCConfig : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {

        registry.addResourceHandler("/styles/css/**")
            .addResourceLocations("classpath:/static/styles/css/")


        registry.addResourceHandler("/images/**", "/user/images/**", "admin/images/**", "admin/departments/images/**", "admin/operations/images/**")
            .addResourceLocations("classpath:/static/images/")
        registry.addResourceHandler("/js/**")
            .addResourceLocations("classpath:/static/js/")

    }
}