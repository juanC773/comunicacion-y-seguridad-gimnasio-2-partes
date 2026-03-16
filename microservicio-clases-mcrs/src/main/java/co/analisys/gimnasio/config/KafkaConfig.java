package co.analisys.gimnasio.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    public static final String TOPIC_OCUPACION_CLASES = "ocupacion-clases";

    @Bean
    public NewTopic topicOcupacionClases() {
        return TopicBuilder.name(TOPIC_OCUPACION_CLASES)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
