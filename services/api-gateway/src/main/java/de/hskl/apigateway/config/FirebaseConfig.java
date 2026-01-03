package de.hskl.apigateway.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    private static Logger logger = LoggerFactory.getLogger(FirebaseConfig.class);

    @Value("${firebase.credentials-path:}")
    private String credentialsPath;

    @Value("${firebase.emulator.enabled:false}")
    private boolean emulatorEnabled;

    @Value("${firebase.emulator.host:localhost:9099}")
    private String emulatorHost;

    @PostConstruct
    public void init() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                FirebaseOptions options;

                if (emulatorEnabled) {
                    // Emulator Mode
                    System.setProperty("FIREBASE_AUTH_EMULATOR_HOST", emulatorHost);
                    options = FirebaseOptions.builder()
                            .setProjectId("local-project")
                            .setCredentials(GoogleCredentials.newBuilder().build())
                            .build();
                    logger.info("Firebase initialized with emulator at {}", emulatorHost);
                } else {
                    // Production Mode
                    FileInputStream serviceAccount = new FileInputStream(credentialsPath);
                    options = FirebaseOptions.builder()
                            .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                            .build();
                    logger.info("Firebase initialized with service account");
                }

                FirebaseApp.initializeApp(options);
            } catch (IOException e) {
                logger.error("Failed to initialize Firebase", e);
                throw new RuntimeException("Firebase initialization failed", e);
            }
        }
    }
}
