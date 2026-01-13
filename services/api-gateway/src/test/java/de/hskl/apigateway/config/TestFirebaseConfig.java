package de.hskl.apigateway.config;

import com.google.firebase.FirebaseApp;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@TestConfiguration
@Profile("test")
public class TestFirebaseConfig {
    @Bean
    @Primary
    public FirebaseApp firebaseApp() {
        if (FirebaseApp.getApps().isEmpty()) {
            return null;
        }
        return FirebaseApp.getInstance();
    }
}
