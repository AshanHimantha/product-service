//package com.ashanhimantha.product_service.config;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
//import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
//import org.springframework.context.EnvironmentAware;
//import org.springframework.core.env.ConfigurableEnvironment;
//import org.springframework.core.env.Environment;
//import org.springframework.core.env.MapPropertySource;
//import org.springframework.stereotype.Component;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
//import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Component
//@Slf4j
//public class SecretsManagerConfig implements BeanFactoryPostProcessor, EnvironmentAware {
//
//    private ConfigurableEnvironment environment;
//
//    @Override
//    public void setEnvironment(Environment environment) {
//        this.environment = (ConfigurableEnvironment) environment;
//    }
//
//    @Override
//    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
//        loadSecretsFromAws();
//    }
//
//    private void loadSecretsFromAws() {
//        try {
//            String awsRegion = environment.getProperty("aws.region", "ap-southeast-2");
//            String secretName = environment.getProperty("aws.secrets.database-secret-name", "ecom-postgres");
//
//
//            try (SecretsManagerClient client = SecretsManagerClient.builder()
//                    .region(Region.of(awsRegion))
//                    .credentialsProvider(DefaultCredentialsProvider.create()) // IAM Role
//                    .build()) {
//
//                GetSecretValueRequest request = GetSecretValueRequest.builder()
//                        .secretId(secretName)
//                        .build();
//
//                GetSecretValueResponse response = client.getSecretValue(request);
//                String secretString = response.secretString();
//
//                ObjectMapper objectMapper = new ObjectMapper();
//                @SuppressWarnings("unchecked")
//                Map<String, Object> secretMap = objectMapper.readValue(secretString, Map.class);
//
//                // Create Spring Boot-compatible DB properties
//                Map<String, Object> properties = new HashMap<>();
//                properties.put("spring.datasource.url",
//                        String.format("jdbc:postgresql://%s:%s/ecom",
//                                secretMap.get("host"), secretMap.get("port")));
//                properties.put("spring.datasource.username", secretMap.get("username"));
//                properties.put("spring.datasource.password", secretMap.get("password"));
//
//                // Add secrets to Spring Environment
//                MapPropertySource secretsPropertySource = new MapPropertySource("awsSecrets", properties);
//                environment.getPropertySources().addFirst(secretsPropertySource);
//
//                log.info("✅ Successfully loaded DB credentials from AWS Secrets Manager");
//            }
//
//        } catch (Exception e) {
//            log.error("❌ Failed to load secrets from AWS Secrets Manager", e);
//            throw new RuntimeException("Failed to initialize application with AWS secrets", e);
//        }
//    }
//}
