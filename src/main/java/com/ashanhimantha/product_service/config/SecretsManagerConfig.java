package com.ashanhimantha.product_service.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SecretsManagerConfig implements BeanFactoryPostProcessor, EnvironmentAware {

    private ConfigurableEnvironment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = (ConfigurableEnvironment) environment;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Check if we should use AWS Secrets Manager or environment variables
        String useSecretsManager = environment.getProperty("aws.secrets.enabled", "true");

        if ("false".equalsIgnoreCase(useSecretsManager)) {
            log.info("🐳 AWS Secrets Manager disabled - using environment variables for database configuration");
            loadSecretsFromEnvironment();
        } else {
            loadSecretsFromAws();
        }
    }

    private void loadSecretsFromEnvironment() {
        try {
            // Database configuration
            String dbHost = environment.getProperty("DB_HOST");
            String dbPort = environment.getProperty("DB_PORT");
            String dbName = environment.getProperty("DB_NAME");
            String dbUsername = environment.getProperty("DB_USERNAME");
            String dbPassword = environment.getProperty("DB_PASSWORD");

            // AWS S3 configuration
            String s3BucketName = environment.getProperty("AWS_S3_BUCKET_NAME");
            String s3BaseUrl = environment.getProperty("AWS_S3_BASE_URL");

            // JWT JWK Set URI
            String jwtJwkSetUri = environment.getProperty("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI");

            Map<String, Object> properties = new HashMap<>();

            // Add database properties
            if (dbHost != null && dbPort != null && dbName != null && dbUsername != null && dbPassword != null) {
                properties.put("spring.datasource.url",
                        String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName));
                properties.put("spring.datasource.username", dbUsername);
                properties.put("spring.datasource.password", dbPassword);
                log.info("✅ Successfully loaded DB credentials from environment variables");
            } else {
                log.warn("⚠️ Some database environment variables are missing");
            }

            // Add S3 properties
            if (s3BucketName != null) {
                properties.put("aws.s3.bucket-name", s3BucketName);
                log.info("✅ Successfully loaded S3 bucket name from environment variables");
            } else {
                log.warn("⚠️ S3 bucket name environment variable is missing");
            }

            if (s3BaseUrl != null) {
                properties.put("aws.s3.base-url", s3BaseUrl);
                log.info("✅ Successfully loaded S3 base URL from environment variables");
            } else {
                log.warn("⚠️ S3 base URL environment variable is missing");
            }

            // Add JWT JWK Set URI
            if (jwtJwkSetUri != null) {
                properties.put("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", jwtJwkSetUri);
                log.info("✅ Successfully loaded JWT JWK Set URI from environment variables");
            } else {
                log.warn("⚠️ JWT JWK Set URI environment variable is missing");
            }

            if (!properties.isEmpty()) {
                MapPropertySource secretsPropertySource = new MapPropertySource("envSecrets", properties);
                environment.getPropertySources().addFirst(secretsPropertySource);
            } else {
                log.warn("⚠️ No environment variables loaded, falling back to properties file");
            }
        } catch (Exception e) {
            log.error("❌ Failed to load secrets from environment variables", e);
            throw new RuntimeException("Failed to initialize application with environment secrets", e);
        }
    }

    private void loadSecretsFromAws() {
        try {
            String awsRegion = environment.getProperty("aws.region");
            String secretName = environment.getProperty("aws.secrets.database-secret-name");

            if (awsRegion == null || secretName == null) {
                log.warn("⚠️ AWS region or secret name not configured, skipping AWS Secrets Manager");
                return;
            }

            try (SecretsManagerClient client = SecretsManagerClient.builder()
                    .region(Region.of(awsRegion))
                    .credentialsProvider(DefaultCredentialsProvider.create()) // IAM Role
                    .build()) {

                GetSecretValueRequest request = GetSecretValueRequest.builder()
                        .secretId(secretName)
                        .build();

                GetSecretValueResponse response = client.getSecretValue(request);
                String secretString = response.secretString();

                ObjectMapper objectMapper = new ObjectMapper();
                @SuppressWarnings("unchecked")
                Map<String, Object> secretMap = objectMapper.readValue(secretString, Map.class);

                // Create Spring Boot-compatible properties
                Map<String, Object> properties = new HashMap<>();

                // Database properties
                String dbName = secretMap.containsKey("database") ?
                        secretMap.get("database").toString() :
                        (secretMap.containsKey("dbname") ? secretMap.get("dbname").toString() : "ecom");

                properties.put("spring.datasource.url",
                        String.format("jdbc:postgresql://%s:%s/%s",
                                secretMap.get("host"), secretMap.get("port"), dbName));
                properties.put("spring.datasource.username", secretMap.get("username"));
                properties.put("spring.datasource.password", secretMap.get("password"));
                log.info("✅ Successfully loaded DB credentials from AWS Secrets Manager");

                // S3 properties
                if (secretMap.containsKey("s3BucketName")) {
                    properties.put("aws.s3.bucket-name", secretMap.get("s3BucketName"));
                    log.info("✅ Successfully loaded S3 bucket name from AWS Secrets Manager");
                }

                if (secretMap.containsKey("s3BaseUrl")) {
                    properties.put("aws.s3.base-url", secretMap.get("s3BaseUrl"));
                    log.info("✅ Successfully loaded S3 base URL from AWS Secrets Manager");
                }

                // JWT JWK Set URI
                if (secretMap.containsKey("jwtJwkSetUri")) {
                    properties.put("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", secretMap.get("jwtJwkSetUri"));
                    log.info("✅ Successfully loaded JWT JWK Set URI from AWS Secrets Manager");
                }

                // Add secrets to Spring Environment
                MapPropertySource secretsPropertySource = new MapPropertySource("awsSecrets", properties);
                environment.getPropertySources().addFirst(secretsPropertySource);

            }

        } catch (Exception e) {
            log.error("❌ Failed to load secrets from AWS Secrets Manager", e);
            throw new RuntimeException("Failed to initialize application with AWS secrets", e);
        }
    }
}
