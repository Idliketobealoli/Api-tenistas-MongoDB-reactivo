package com.example.tennislabspringboot.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class APIConfig {
    companion object {
        @Value("\${api.path}")
        const val API_PATH = "/rest"
    }
}