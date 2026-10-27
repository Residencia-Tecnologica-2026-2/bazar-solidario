
package com.isac.bazar_solidario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path pastaUploads = Paths.get("uploads")
                .toAbsolutePath()
                .normalize();

        System.out.println("Pasta de imagens: " + pastaUploads);

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(pastaUploads.toUri().toString());
    }
}
