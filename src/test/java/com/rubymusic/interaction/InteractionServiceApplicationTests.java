package com.rubymusic.interaction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class InteractionServiceApplicationTests {

    /**
     * KafkaAutoConfiguration is excluded in application-test.yml.
     * SongInteractionServiceImpl requires KafkaTemplate — mock it so the context loads.
     */
    @MockBean
    @SuppressWarnings("rawtypes")
    KafkaTemplate kafkaTemplate;

    @Test
    void contextLoads() {
    }
}
