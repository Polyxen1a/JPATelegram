package pro.sky.jpatelegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableSheduling

public class JpaTelegramApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaTelegramApplication.class, args);
    }

}
