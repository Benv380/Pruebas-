package cl.zona_ti.compra_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CompraServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompraServiceApplication.class, args);
    }
}
