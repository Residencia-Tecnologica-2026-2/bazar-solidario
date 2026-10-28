
package com.isac.bazar_solidario.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadWebConfig implements WebMvcConfigurer {

    @Value("${app.uploads.directory:uploads}")
    private String diretorioUploads;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path pastaUploads = Paths.get(diretorioUploads)
                .toAbsolutePath()
                .normalize();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(pastaUploads.toUri().toString());
    }
}
