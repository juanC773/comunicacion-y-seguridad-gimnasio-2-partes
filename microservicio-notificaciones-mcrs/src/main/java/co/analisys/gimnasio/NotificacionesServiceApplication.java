package co.analisys.gimnasio;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRabbit
public class NotificacionesServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificacionesServiceApplication.class, args);
	}
}
