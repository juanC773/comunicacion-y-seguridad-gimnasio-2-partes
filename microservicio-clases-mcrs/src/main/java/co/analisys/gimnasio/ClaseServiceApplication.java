package co.analisys.gimnasio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@SpringBootApplication
public class ClaseServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ClaseServiceApplication.class, args);
	}

	@Bean
	@LoadBalanced
	public RestTemplate restTemplate() {
		RestTemplate restTemplate = new RestTemplate();
		restTemplate.setInterceptors(Collections.singletonList(jwtPropagationInterceptor()));
		return restTemplate;
	}

	private ClientHttpRequestInterceptor jwtPropagationInterceptor() {
		return (request, body, execution) -> {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
				request.getHeaders().setBearerAuth(jwt.getTokenValue());
			}
			return execution.execute(request, body);
		};
	}
}
