package com.nhattung.promotionservice.config;

import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;


@Configuration
public class ShopConfig {

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

}
