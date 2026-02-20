package org.nurfet.paymentstrackingsystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI utilityPaymentsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Utility Payments Tracking System API")
                        .description("""
                                REST API системы учёта коммунальных платежей.
                                
                                Система позволяет:
                                - управлять лицевыми счетами по 8 видам коммунальных услуг
                                - регистрировать платежи
                                - передавать показания счётчиков (для услуг с приборами учёта)
                                - отслеживать статус оплаты за текущий месяц
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Utility Payments Team")
                                .email("support@utilpay.example.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8888")
                                .description("Локальный сервер разработки")
                ));
    }
}
