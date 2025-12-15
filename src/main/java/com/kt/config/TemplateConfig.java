package com.kt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;

@Configuration
public class TemplateConfig {
    @Bean
    public TemplateEngine htmlTemplateEngine(SpringResourceTemplateResolver springResourceTemplateResolver) {
        TemplateEngine templateEngine = new TemplateEngine();
        templateEngine.addTemplateResolver(springResourceTemplateResolver);
        return templateEngine;
    }
}
