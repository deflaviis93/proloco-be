package it.def.prolocobe;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
public class ProlocoBeApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProlocoBeApplication.class, args);
    }
}
