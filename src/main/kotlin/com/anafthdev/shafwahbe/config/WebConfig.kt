package com.anafthdev.shafwahbe.config

import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/api/**") // Terapkan pada semua endpoint di bawah /api/
            .allowedOrigins(
                "http://localhost:60000", // Port run flutter web
                "http://localhost:9999"  // Kadang diperlukan untuk beberapa kasus
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
    }
}