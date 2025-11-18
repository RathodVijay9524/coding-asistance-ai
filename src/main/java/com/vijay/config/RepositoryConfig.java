package com.vijay.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 🗄️ REPOSITORY CONFIGURATION
 * 
 * Enables Spring Data JPA repositories and transaction management.
 * Scans for repository interfaces in the com.vijay.repository package.
 * 
 * ✅ PHASE 3: Advanced Features - Week 11
 */
@Configuration
@EnableJpaRepositories(
        basePackages = "com.vijay.repository",
        entityManagerFactoryRef = "entityManagerFactory",
        transactionManagerRef = "transactionManager"
)
@EnableTransactionManagement
public class RepositoryConfig {
    
    // Configuration is handled by Spring Boot auto-configuration
    // This class explicitly enables JPA repositories and transaction management
    
}
