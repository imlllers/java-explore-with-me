package ru.yandex.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"ru.yandex.practicum", "ru.practicum.client"})
public class ExploreWithMeApplication {
    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeApplication.class, args);
    }
}
