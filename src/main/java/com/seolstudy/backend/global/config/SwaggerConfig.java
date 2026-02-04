package com.seolstudy.backend.global.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
        info = @Info(title = "설스터디(수미상관 팀) Backend API",
                version = "v1",
                description = "API 명세서"))
@Configuration
public class SwaggerConfig {

}
