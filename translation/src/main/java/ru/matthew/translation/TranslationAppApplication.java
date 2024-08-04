package ru.matthew.translation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"ru.matthew.translation", "ru.matthew.auth"})
public class TranslationAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(TranslationAppApplication.class, args);
	}
}
