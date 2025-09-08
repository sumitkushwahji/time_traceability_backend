# ⚙️ Configuration Guide

Complete configuration reference for the Time Traceability Backend system covering all configuration options, properties, and environment-specific settings.

## 📋 Table of Contents

- [Configuration Overview](#-configuration-overview)
- [Application Properties](#-application-properties)
- [Database Configuration](#-database-configuration)
- [File Processing Configuration](#-file-processing-configuration)
- [Scheduler Configuration](#-scheduler-configuration)
- [Logging Configuration](#-logging-configuration)
- [Security Configuration](#-security-configuration)
- [Performance Configuration](#-performance-configuration)
- [Environment-Specific Configurations](#-environment-specific-configurations)
- [External Configuration](#-external-configuration)
- [Configuration Validation](#-configuration-validation)
- [Advanced Configuration](#-advanced-configuration)

## 🎯 Configuration Overview

The Time Traceability Backend uses Spring Boot's configuration system with support for:

- **Property Files**: `application.properties` and profile-specific variants
- **Environment Variables**: Override any property using environment variables
- **Command Line Arguments**: Runtime property overrides
- **External Configuration**: External configuration files and services

### Configuration Hierarchy (Priority Order)
1. Command line arguments
2. Environment variables
3. Profile-specific properties (`application-{profile}.properties`)
4. Default properties (`application.properties`)

## 📝 Application Properties

### Core Application Settings

```properties
# Application Information
spring.application.name=time-traceability-backend
app.version=@project.version@
app.description=IRNSS Satellite Data Processing Backend

# Server Configuration
server.port=6003
server.servlet.context-path=/time-traceability-service
server.servlet.session.timeout=30m
server.tomcat.max-threads=200
server.tomcat.min-spare-threads=10

# Application Profile
spring.profiles.active=dev

# Graceful Shutdown
server.shutdown=graceful
spring.lifecycle.timeout-per-shutdown-phase=30s
```

### Spring Boot Actuator

```properties
# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoints.web.base-path=/actuator
management.endpoint.health.show-details=when_authorized
management.endpoint.health.show-components=always

# Health Check Configuration
management.health.db.enabled=true
management.health.diskspace.enabled=true
management.health.diskspace.threshold=10GB

# Info Endpoint
management.info.build.enabled=true
management.info.git.enabled=true
management.info.env.enabled=true

# Metrics Configuration
management.metrics.export.prometheus.enabled=true
management.metrics.distribution.percentiles-histogram.http.server.requests=true
```

## 🗄️ Database Configuration

### Basic Database Settings

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=traceability_user
spring.datasource.password=${DB_PASSWORD:default_password}
spring.datasource.driver-class-name=org.postgresql.Driver

# Connection Pool (HikariCP)
spring.datasource.hikari.pool-name=TimeTraceabilityPool
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=900000
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.validation-timeout=5000
spring.datasource.hikari.leak-detection-threshold=60000

# Connection Pool Monitoring
spring.datasource.hikari.register-mbeans=true
```

### JPA/Hibernate Configuration

```properties
# JPA Configuration
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.hibernate.naming.physical-strategy=org.hibernate.boot.model.naming.PhysicalNamingStrategyStandardImpl

# Hibernate Properties
spring.jpa.properties.hibernate.jdbc.batch_size=25
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true
spring.jpa.properties.hibernate.generate_statistics=false

# SQL Logging (disable in production)
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN

# Database Initialization
spring.jpa.defer-datasource-initialization=true
spring.sql.init.mode=never
```

### Advanced Database Settings

```properties
# Transaction Configuration
spring.transaction.default-timeout=300
spring.transaction.rollback-on-commit-failure=true

# Database Connection Validation
spring.datasource.hikari.connection-test-query=SELECT 1
spring.datasource.hikari.test-while-idle=true
spring.datasource.hikari.test-on-borrow=true

# PostgreSQL Specific Settings
spring.datasource.hikari.data-source-properties.stringtype=unspecified
spring.datasource.hikari.data-source-properties.reWriteBatchedInserts=true
spring.datasource.hikari.data-source-properties.prepareThreshold=1
```

## 📁 File Processing Configuration

### Core File Processing Settings

```properties
# File Processing Configuration
irnss.parent-folder=${DATA_FOLDER:/opt/satellite/data}
irnss.processing.enabled=true
irnss.processing.batch-size=100
irnss.processing.max-retries=3
irnss.processing.retry-delay-ms=5000
irnss.processing.parallel-directories=true
irnss.processing.max-threads=4

# File Monitoring
irnss.monitoring.watch-service-enabled=false
irnss.monitoring.scan-interval-ms=300000
irnss.monitoring.file-age-threshold-minutes=5

# File Validation
irnss.validation.enabled=true
irnss.validation.max-file-size-mb=100
irnss.validation.allowed-extensions=.24d,.nav,.obs
irnss.validation.min-file-size-bytes=100

# Character Encoding
irnss.encoding.primary=UTF-8
irnss.encoding.fallback=ISO-8859-1
irnss.encoding.auto-detect=true
```

### File Processing Performance

```properties
# Performance Tuning
irnss.performance.buffer-size-kb=64
irnss.performance.io-timeout-seconds=30
irnss.performance.memory-threshold-mb=512
irnss.performance.gc-after-files=100

# Error Handling
irnss.error-handling.skip-corrupted-files=true
irnss.error-handling.max-errors-per-file=10
irnss.error-handling.continue-on-error=true
irnss.error-handling.quarantine-bad-files=false

# Progress Tracking
irnss.progress.update-interval-lines=1000
irnss.progress.persist-interval-seconds=60
irnss.progress.cleanup-completed-after-days=30
```

## ⏰ Scheduler Configuration

### Main Scheduler Settings

```properties
# Scheduler Configuration
irnss.scheduler.enabled=true
irnss.scheduler.thread-pool-size=5
irnss.scheduler.await-termination-seconds=30

# File Processing Scheduler
irnss.scheduler.file-processing.enabled=true
irnss.scheduler.file-processing.fixed-rate=300000
irnss.scheduler.file-processing.initial-delay=60000
irnss.scheduler.file-processing.max-duration-minutes=30

# Materialized View Scheduler
app.scheduler.materialized-view.enabled=true
app.scheduler.materialized-view.refresh-interval=300000
app.scheduler.materialized-view.initial-delay=120000
app.scheduler.materialized-view.timeout-minutes=10
app.scheduler.materialized-view.names=sat_common_view_difference_materialized,sat_pivoted_view_materialized,sat_combined_view_difference_materialized

# Cleanup Scheduler
app.scheduler.cleanup.enabled=true
app.scheduler.cleanup.old-files-cron=0 2 * * * *
app.scheduler.cleanup.retention-days=90
app.scheduler.cleanup.batch-size=1000
```

### Advanced Scheduler Configuration

```properties
# Scheduler Monitoring
app.scheduler.monitoring.enabled=true
app.scheduler.monitoring.metrics-enabled=true
app.scheduler.monitoring.alert-on-failure=true

# Error Recovery
app.scheduler.error-recovery.max-retries=3
app.scheduler.error-recovery.backoff-multiplier=2.0
app.scheduler.error-recovery.max-delay-seconds=300

# Concurrency Control
app.scheduler.concurrency.file-processing-locks=true
app.scheduler.concurrency.view-refresh-locks=true
app.scheduler.concurrency.lock-timeout-seconds=600
```

## 📊 Logging Configuration

### Basic Logging Setup

```properties
# Root Logging Level
logging.level.root=INFO

# Application Logging
logging.level.com.time.tracealibility=INFO
logging.level.com.time.tracealibility.services=DEBUG
logging.level.com.time.tracealibility.scheduler=INFO

# Framework Logging
logging.level.org.springframework=WARN
logging.level.org.springframework.web=INFO
logging.level.org.springframework.security=WARN
logging.level.org.hibernate=WARN

# Database Logging
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql=WARN
logging.level.com.zaxxer.hikari=WARN

# File and Console Logging
logging.file.name=logs/application.log
logging.file.max-size=100MB
logging.file.max-history=30
logging.file.total-size-cap=1GB

# Log Pattern
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### Advanced Logging Configuration

```properties
# Structured Logging (JSON)
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.charset.console=UTF-8
logging.charset.file=UTF-8

# Async Logging
logging.config=classpath:logback-spring.xml

# MDC (Mapped Diagnostic Context)
app.logging.mdc.include-user=true
app.logging.mdc.include-session=true
app.logging.mdc.include-request-id=true

# Performance Logging
app.logging.performance.enabled=true
app.logging.performance.slow-query-threshold-ms=1000
app.logging.performance.slow-request-threshold-ms=2000
```

## 🔒 Security Configuration

### CORS Configuration

```properties
# CORS Configuration
app.cors.enabled=true
app.cors.allowed-origins=http://localhost:4200,http://127.0.0.1:4200,https://your-frontend-domain.com
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS,PATCH
app.cors.allowed-headers=*
app.cors.exposed-headers=X-Total-Count,X-Total-Pages
app.cors.allow-credentials=true
app.cors.max-age=3600

# API Security (when enabled)
app.security.enabled=false
app.security.api-key.enabled=false
app.security.api-key.header-name=X-API-Key
app.security.api-key.valid-keys=${API_KEYS:}

# Rate Limiting
app.rate-limiting.enabled=false
app.rate-limiting.requests-per-minute=60
app.rate-limiting.burst-capacity=100
```

### SSL/TLS Configuration (when enabled)

```properties
# SSL Configuration
server.ssl.enabled=false
server.ssl.key-store-type=PKCS12
server.ssl.key-store=${SSL_KEYSTORE_PATH:classpath:keystore.p12}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD:}
server.ssl.key-alias=time-traceability-backend

# SSL Security
server.ssl.protocol=TLS
server.ssl.enabled-protocols=TLSv1.2,TLSv1.3
server.ssl.ciphers=TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256
```

## ⚡ Performance Configuration

### JVM Performance Settings

```properties
# JVM Configuration (set via environment variables or startup script)
# JAVA_OPTS=-Xmx4g -Xms2g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+PrintGC

# Application Performance
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.cache.use_second_level_cache=false
spring.jpa.properties.hibernate.cache.use_query_cache=false

# Connection Pool Performance
spring.datasource.hikari.maximum-pool-size=25
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000

# Thread Pool Configuration
server.tomcat.threads.max=200
server.tomcat.threads.min-spare=10
server.tomcat.accept-count=100
server.tomcat.max-connections=8192

# Async Processing
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=16
spring.task.execution.pool.queue-capacity=100
spring.task.execution.thread-name-prefix=async-task-
```

### Caching Configuration

```properties
# Caching Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=10m

# Custom Cache Configuration
app.cache.enabled=true
app.cache.mjd-cache.size=500
app.cache.mjd-cache.ttl-minutes=30
app.cache.statistics-cache.size=100
app.cache.statistics-cache.ttl-minutes=5
```

## 🌍 Environment-Specific Configurations

### Development Environment (`application-dev.properties`)

```properties
# Development Configuration
spring.profiles.active=dev
server.port=6003

# Database
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability_dev
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# File Processing
irnss.parent-folder=C:/dev/satellite_data
irnss.processing.batch-size=10
irnss.scheduler.file-processing.fixed-rate=60000

# Logging
logging.level.com.time.tracealibility=DEBUG
logging.level.org.springframework.web=DEBUG

# Development Tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.h2.console.enabled=false
```

### Testing Environment (`application-test.properties`)

```properties
# Test Configuration
spring.profiles.active=test

# In-Memory Database for Testing
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop

# Disable Schedulers
irnss.scheduler.enabled=false
app.scheduler.materialized-view.enabled=false

# Test Data
irnss.parent-folder=src/test/resources/test-data
irnss.processing.batch-size=5

# Fast Testing
spring.jpa.show-sql=false
logging.level.org.springframework=WARN
```

### Quality Assurance (`application-qa.properties`)

```properties
# QA Configuration
spring.profiles.active=qa
server.port=6003

# QA Database
spring.datasource.url=jdbc:postgresql://qa-db-server:5432/time_traceability_qa
spring.datasource.username=${DB_USER:traceability_user}
spring.datasource.password=${DB_PASSWORD}

# QA File Processing
irnss.parent-folder=/opt/qa/satellite_data
irnss.processing.batch-size=100
irnss.scheduler.file-processing.fixed-rate=180000

# QA Logging
logging.level.com.time.tracealibility=INFO
logging.file.name=/var/log/time-traceability-qa/application.log

# Monitoring
management.endpoints.web.exposure.include=health,info,metrics
```

### Production Environment (`application-prod.properties`)

```properties
# Production Configuration
spring.profiles.active=prod
server.port=6003

# Production Database
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# Production File Processing
irnss.parent-folder=${DATA_FOLDER:/opt/satellite/data}
irnss.processing.batch-size=500
irnss.processing.parallel-directories=true
irnss.scheduler.file-processing.fixed-rate=300000

# Production Logging
logging.level.com.time.tracealibility=INFO
logging.level.root=WARN
logging.file.name=/var/log/time-traceability/application.log
logging.file.max-size=200MB
logging.file.max-history=60

# Production Security
app.cors.allowed-origins=${ALLOWED_ORIGINS:https://prod-frontend.com}
app.security.enabled=true

# Production Monitoring
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=never
```

## 🔗 External Configuration

### Environment Variables

```bash
# Database Environment Variables
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=time_traceability
export DB_USER=traceability_user
export DB_PASSWORD=secure_password

# Application Environment Variables
export DATA_FOLDER=/opt/satellite/data
export JAVA_OPTS="-Xmx4g -Xms2g -XX:+UseG1GC"
export SPRING_PROFILES_ACTIVE=prod

# Security Environment Variables
export API_KEYS=key1,key2,key3
export ALLOWED_ORIGINS=https://frontend1.com,https://frontend2.com
export SSL_KEYSTORE_PATH=/etc/ssl/keystore.p12
export SSL_KEYSTORE_PASSWORD=keystorepassword
```

### Configuration via Command Line

```bash
# Override properties via command line
java -jar app.jar \
  --spring.profiles.active=prod \
  --server.port=8080 \
  --spring.datasource.url=jdbc:postgresql://prod-db:5432/time_traceability \
  --irnss.parent-folder=/data/satellite \
  --logging.level.com.time.tracealibility=DEBUG
```

### External Configuration Files

```bash
# Use external configuration directory
java -jar app.jar --spring.config.additional-location=/etc/time-traceability/

# Use specific external configuration file
java -jar app.jar --spring.config.location=file:/etc/time-traceability/application.properties
```

## ✅ Configuration Validation

### Startup Validation

The application validates configuration at startup:

```java
@ConfigurationProperties(prefix = "irnss")
@Validated
public class IrnssConfigurationProperties {
    
    @NotBlank
    @ValidPath
    private String parentFolder;
    
    @Positive
    @Max(10000)
    private int batchSize = 100;
    
    @Positive
    @Max(10)
    private int maxRetries = 3;
}
```

### Configuration Health Checks

```properties
# Configuration Validation
app.validation.config.enabled=true
app.validation.config.fail-fast=true
app.validation.config.check-directories=true
app.validation.config.check-database=true

# Health Checks for Configuration
management.health.config.enabled=true
management.health.diskspace.path=${irnss.parent-folder}
management.health.db.enabled=true
```

## 🔧 Advanced Configuration

### Custom Configuration Beans

```java
@Configuration
@EnableConfigurationProperties({
    IrnssConfigurationProperties.class,
    SchedulerConfigurationProperties.class
})
public class ApplicationConfiguration {
    
    @Bean
    @ConfigurationProperties(prefix = "app.custom")
    public CustomProperties customProperties() {
        return new CustomProperties();
    }
}
```

### Conditional Configuration

```properties
# Conditional Configuration
app.features.materialized-views.enabled=true
app.features.file-watching.enabled=${irnss.monitoring.watch-service-enabled:false}
app.features.async-processing.enabled=true
app.features.metrics.enabled=${management.metrics.export.prometheus.enabled:false}

# Feature Flags
app.experimental.new-parser.enabled=false
app.experimental.parallel-db-writes.enabled=false
app.experimental.compression.enabled=false
```

### Configuration Profiles for Different Deployments

```properties
# Kubernetes Deployment
spring.profiles.active=k8s
app.deployment.type=kubernetes
app.deployment.namespace=${KUBERNETES_NAMESPACE:default}
app.deployment.pod-name=${HOSTNAME:unknown}

# Docker Deployment
spring.profiles.active=docker
app.deployment.type=docker
app.deployment.container-id=${HOSTNAME:unknown}

# Standalone Deployment
spring.profiles.active=standalone
app.deployment.type=standalone
app.deployment.server-name=${HOSTNAME:unknown}
```

### Configuration Encryption

```properties
# Encrypted Properties (using jasypt)
spring.datasource.password=ENC(encrypted_password_here)
app.security.api-key.master-key=ENC(encrypted_master_key_here)

# Encryption Configuration
jasypt.encryptor.algorithm=PBEWithMD5AndDES
jasypt.encryptor.password=${JASYPT_PASSWORD:}
```

## 📋 Configuration Best Practices

### 1. Security Best Practices
- Never commit passwords or sensitive data to version control
- Use environment variables for sensitive configuration
- Implement configuration encryption for production
- Validate all external inputs

### 2. Performance Best Practices
- Tune connection pool sizes based on load
- Configure appropriate timeouts
- Enable caching where beneficial
- Monitor configuration impact on performance

### 3. Operational Best Practices
- Use meaningful configuration property names
- Document all custom configuration properties
- Implement configuration validation
- Provide sensible defaults

### 4. Environment Best Practices
- Use profiles for different environments
- Externalize environment-specific configuration
- Implement configuration drift detection
- Maintain configuration documentation

---

## 📞 Configuration Support

For configuration assistance:

1. **Validation Errors**: Check startup logs for configuration validation messages
2. **Performance Issues**: Review performance-related configuration settings
3. **Environment Problems**: Verify environment-specific configuration files
4. **Advanced Configuration**: Consult the development team for custom requirements

**Related Documentation**: 
- [Installation Guide](Installation-Guide.md) for initial setup
- [Environment Setup](Environment-Setup.md) for development environment configuration
- [Performance Optimization](Performance-Optimization.md) for performance tuning

---

**Configuration Complete!** 🎉 Your Time Traceability Backend is properly configured for your environment.
