package budgetapprefactored;

import org.jetbrains.annotations.NotNull;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
public class BudgetAppRefactoredApplication {

	public static void main(String[] args) {
		SpringApplication.run(BudgetAppRefactoredApplication.class, args);
	}
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(@NotNull CorsRegistry registry) {
				registry.addMapping("/api/**").allowedOrigins("http://192.168.1.134:5173", "http://localhost:5173").allowCredentials(true);
			}
		};
	}
}
