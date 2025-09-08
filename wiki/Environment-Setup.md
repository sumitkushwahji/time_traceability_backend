# 🔧 Environment Setup

Complete guide for setting up development environments for the Time Traceability Backend system, including IDE configuration, development tools, and local development workflows.

## 📋 Table of Contents

- [Development Environment Overview](#-development-environment-overview)
- [IDE Setup](#-ide-setup)
- [Local Development Setup](#-local-development-setup)
- [Database Development Environment](#-database-development-environment)
- [Development Tools](#-development-tools)
- [Testing Environment](#-testing-environment)
- [Debug Configuration](#-debug-configuration)
- [Hot Reload Setup](#-hot-reload-setup)
- [Development Workflows](#-development-workflows)
- [Common Development Issues](#-common-development-issues)

## 🎯 Development Environment Overview

The development environment for Time Traceability Backend includes:

- **Primary IDE**: IntelliJ IDEA or Visual Studio Code
- **Build Tool**: Maven 3.6+
- **Runtime**: Java 17+
- **Database**: PostgreSQL 12+ (local instance)
- **Version Control**: Git
- **Testing**: JUnit 5, Testcontainers, H2 (for unit tests)

### Environment Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   IDE/Editor    │    │  Local Database │    │   File System   │
│                 │    │                 │    │                 │
│ • IntelliJ IDEA │───▶│ • PostgreSQL    │───▶│ • Test Data     │
│ • VS Code       │    │ • H2 (testing)  │    │ • Logs          │
│ • Debugging     │    │ • pgAdmin       │    │ • Satellite Data│
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                        │                        │
         └────────────────────────┼────────────────────────┘
                                  │
                    ┌─────────────────┐
                    │  Spring Boot    │
                    │  Dev Server     │
                    │  Port: 6003     │
                    └─────────────────┘
```

## 💻 IDE Setup

### IntelliJ IDEA Setup

#### 1. Import Project
```bash
# Clone repository
git clone https://github.com/sumitkushwahji/time_traceability_backend.git
cd time_traceability_backend

# Open in IntelliJ IDEA
# File → Open → Select project folder
# Choose "Import as Maven project" if prompted
```

#### 2. Configure Project SDK
```
File → Project Structure → Project Settings → Project
- Project SDK: Java 17 (or latest)
- Project language level: 17
- Project compiler output: target/classes
```

#### 3. Configure Maven
```
File → Settings → Build, Execution, Deployment → Build Tools → Maven
- Maven home directory: /path/to/maven (or use bundled)
- User settings file: ~/.m2/settings.xml
- Local repository: ~/.m2/repository

Import Maven projects automatically: ✓
```

#### 4. Configure Code Style
```
File → Settings → Editor → Code Style → Java
- Scheme: Copy from Default
- Rename to: "Time Traceability Style"

Tabs and Indents:
- Use tab character: ✗
- Tab size: 4
- Indent: 4
- Continuation indent: 8

Wrapping and Braces:
- Keep when reformatting → Line breaks: ✓
- Keep maximum blank lines: 2
```

#### 5. Essential Plugins
```
File → Settings → Plugins → Marketplace

Install:
- Lombok Plugin
- SonarLint
- Database Navigator (or Database Tools and SQL)
- GitToolBox
- Rainbow Brackets
- Grep Console
- Maven Helper
- Spring Boot Assistant
```

#### 6. Run/Debug Configuration
```
Run → Edit Configurations → Add New → Spring Boot

Configuration:
- Name: Time Traceability Backend (Dev)
- Main class: com.time.tracealibility.TracealibilityApplication
- Active profiles: dev
- VM options: -Xmx2g -Xms1g -XX:+UseG1GC
- Program arguments: --spring.profiles.active=dev
- Working directory: $PROJECT_DIR$
- Environment variables:
  - DB_PASSWORD=your_db_password
  - DATA_FOLDER=C:/dev/satellite_data (Windows) or /tmp/satellite_data (Linux/Mac)
```

### Visual Studio Code Setup

#### 1. Required Extensions
```json
{
  "recommendations": [
    "redhat.java",
    "vscjava.vscode-java-pack",
    "pivotal.vscode-spring-boot",
    "vscjava.vscode-spring-initializr",
    "vscjava.vscode-spring-boot-dashboard",
    "ms-vscode.vscode-json",
    "eamodio.gitlens",
    "bradlc.vscode-tailwindcss",
    "ms-vscode.remote-containers"
  ]
}
```

#### 2. Workspace Settings (`.vscode/settings.json`)
```json
{
  "java.home": "/path/to/java-17",
  "java.configuration.maven.userSettings": "~/.m2/settings.xml",
  "java.import.maven.enabled": true,
  "java.format.settings.url": ".vscode/java-formatter.xml",
  "spring-boot.ls.java.home": "/path/to/java-17",
  "files.exclude": {
    "**/target": true,
    "**/.classpath": true,
    "**/.project": true,
    "**/.settings": true,
    "**/.factorypath": true
  },
  "java.compile.nullAnalysis.mode": "automatic",
  "spring.initializr.defaultLanguage": "Java",
  "editor.formatOnSave": true,
  "editor.tabSize": 4,
  "editor.insertSpaces": true
}
```

#### 3. Launch Configuration (`.vscode/launch.json`)
```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "Time Traceability Backend (Dev)",
      "request": "launch",
      "mainClass": "com.time.tracealibility.TracealibilityApplication",
      "projectName": "tracealibility",
      "args": "--spring.profiles.active=dev",
      "vmArgs": "-Xmx2g -Xms1g -XX:+UseG1GC",
      "env": {
        "DB_PASSWORD": "your_db_password",
        "DATA_FOLDER": "/tmp/satellite_data"
      },
      "console": "integratedTerminal"
    },
    {
      "type": "java",
      "name": "Debug Time Traceability Backend",
      "request": "launch",
      "mainClass": "com.time.tracealibility.TracealibilityApplication",
      "projectName": "tracealibility",
      "args": "--spring.profiles.active=dev --debug",
      "vmArgs": "-Xmx2g -Xms1g -XX:+UseG1GC -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005",
      "env": {
        "DB_PASSWORD": "your_db_password",
        "DATA_FOLDER": "/tmp/satellite_data"
      },
      "console": "integratedTerminal"
    }
  ]
}
```

#### 4. Tasks Configuration (`.vscode/tasks.json`)
```json
{
  "version": "2.0.0",
  "tasks": [
    {
      "label": "Maven Clean Install",
      "type": "shell",
      "command": "./mvnw",
      "args": ["clean", "install"],
      "group": "build",
      "presentation": {
        "reveal": "always",
        "panel": "new"
      },
      "problemMatcher": "$tsc"
    },
    {
      "label": "Maven Test",
      "type": "shell",
      "command": "./mvnw",
      "args": ["test"],
      "group": "test",
      "presentation": {
        "reveal": "always",
        "panel": "new"
      }
    },
    {
      "label": "Spring Boot Run (Dev)",
      "type": "shell",
      "command": "./mvnw",
      "args": ["spring-boot:run", "-Dspring.profiles.active=dev"],
      "group": "build",
      "isBackground": true,
      "presentation": {
        "reveal": "always",
        "panel": "new"
      }
    }
  ]
}
```

## 🏠 Local Development Setup

### 1. Create Development Directory Structure
```bash
# Create main development directory
mkdir -p ~/dev/time-traceability
cd ~/dev/time-traceability

# Clone repository
git clone https://github.com/sumitkushwahji/time_traceability_backend.git
cd time_traceability_backend

# Create development data directories
mkdir -p dev-data/satellite_data/{IRLMF,IRXYZ,IRABC}
mkdir -p dev-data/logs
mkdir -p dev-data/backup

# Create test files for development
echo "1 123456.789 123456.790 123456.791" > dev-data/satellite_data/IRLMF/GZLI2P60.866
echo "2 123457.789 123457.790 123457.791" > dev-data/satellite_data/IRLMF/GZLI2P60.867
```

### 2. Development Properties File
Create `src/main/resources/application-dev.properties`:

```properties
# Development Server Configuration
server.port=6003
server.servlet.context-path=/time-traceability-service

# Development Database
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability_dev
spring.datasource.username=dev_user
spring.datasource.password=dev_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Development File Processing
irnss.parent-folder=${user.home}/dev/time-traceability/time_traceability_backend/dev-data/satellite_data
irnss.processing.batch-size=10
irnss.processing.parallel-directories=false
irnss.scheduler.file-processing.fixed-rate=60000
irnss.scheduler.file-processing.initial-delay=10000

# Development Logging
logging.level.com.time.tracealibility=DEBUG
logging.level.org.springframework.web=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.file.name=dev-data/logs/application.log

# Development Tools
spring.devtools.restart.enabled=true
spring.devtools.livereload.enabled=true
spring.devtools.restart.exclude=static/**,public/**,dev-data/**

# Development CORS (allow all for development)
app.cors.allowed-origins=http://localhost:*,http://127.0.0.1:*
app.cors.allowed-methods=*
app.cors.allowed-headers=*

# Development Actuator
management.endpoints.web.exposure.include=*
management.endpoint.health.show-details=always
```

### 3. Development Environment Script
Create `dev-setup.sh`:

```bash
#!/bin/bash

# Development Environment Setup Script
echo "Setting up Time Traceability Backend development environment..."

# Check Java version
java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "Java version: $java_version"

if [[ ! "$java_version" =~ ^1[1-9]|^[2-9][0-9] ]]; then
    echo "Error: Java 11+ required, found $java_version"
    exit 1
fi

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven not found"
    exit 1
fi

# Check PostgreSQL
if ! command -v psql &> /dev/null; then
    echo "Warning: PostgreSQL client not found"
fi

# Set development environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_PASSWORD=dev_password
export DATA_FOLDER=$(pwd)/dev-data/satellite_data

# Create development database
echo "Creating development database..."
psql -U postgres -c "CREATE DATABASE time_traceability_dev;" 2>/dev/null
psql -U postgres -c "CREATE USER dev_user WITH PASSWORD 'dev_password';" 2>/dev/null
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE time_traceability_dev TO dev_user;" 2>/dev/null

# Build project
echo "Building project..."
./mvnw clean compile

echo "Development environment setup complete!"
echo "Run: ./mvnw spring-boot:run -Dspring.profiles.active=dev"
```

## 🗄️ Database Development Environment

### 1. Local PostgreSQL Setup for Development

#### Quick Development Database Setup
```bash
# Create development database and user
psql -U postgres << EOF
CREATE DATABASE time_traceability_dev;
CREATE USER dev_user WITH PASSWORD 'dev_password';
GRANT ALL PRIVILEGES ON DATABASE time_traceability_dev TO dev_user;
\c time_traceability_dev
GRANT ALL ON SCHEMA public TO dev_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO dev_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO dev_user;
EOF
```

#### Development Database Configuration
```bash
# PostgreSQL configuration for development
# Edit postgresql.conf (usually in /etc/postgresql/13/main/)
sudo nano /etc/postgresql/13/main/postgresql.conf

# Add/modify these settings:
log_statement = 'all'                   # Log all SQL statements
log_duration = on                       # Log query duration
log_min_duration_statement = 0          # Log all queries
shared_preload_libraries = 'pg_stat_statements'
```

### 2. pgAdmin Setup for Development
```bash
# Install pgAdmin (Ubuntu/Debian)
sudo apt install pgadmin4

# Or use Docker
docker run -p 80:80 \
    -e 'PGADMIN_DEFAULT_EMAIL=dev@example.com' \
    -e 'PGADMIN_DEFAULT_PASSWORD=devpassword' \
    -d dpage/pgadmin4

# Connect to: http://localhost
# Add server:
# - Name: Time Traceability Dev
# - Host: localhost
# - Port: 5432
# - Database: time_traceability_dev
# - Username: dev_user
# - Password: dev_password
```

### 3. Database Development Scripts

Create `dev-scripts/db-reset.sh`:
```bash
#!/bin/bash
# Reset development database

echo "Resetting development database..."

# Drop and recreate database
psql -U postgres -c "DROP DATABASE IF EXISTS time_traceability_dev;"
psql -U postgres -c "CREATE DATABASE time_traceability_dev;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE time_traceability_dev TO dev_user;"

# Restart application to recreate schema
echo "Database reset complete. Restart the application to recreate schema."
```

Create `dev-scripts/db-sample-data.sql`:
```sql
-- Sample data for development
INSERT INTO processed_file (file_name, file_path, processed_at, file_size, record_count)
VALUES 
    ('GZLI2P60.866', '/test/IRLMF/GZLI2P60.866', NOW(), 1024, 10),
    ('GZLI2P60.867', '/test/IRLMF/GZLI2P60.867', NOW(), 2048, 20);

INSERT INTO irnss_data (source, mjd, observation_value, file_name, line_number)
VALUES
    ('GZLI2P', 60866, 123456.789, 'GZLI2P60.866', 1),
    ('GZLI2P', 60866, 123456.790, 'GZLI2P60.866', 2),
    ('GZLI2P', 60867, 123457.789, 'GZLI2P60.867', 1);
```

## 🛠️ Development Tools

### 1. Maven Wrapper Commands for Development
```bash
# Quick development build (skip tests)
./mvnw clean compile -DskipTests

# Run with development profile
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Run tests only
./mvnw test

# Run specific test class
./mvnw test -Dtest=IrnssDataServiceTest

# Run with debug mode
./mvnw spring-boot:run -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Generate test coverage report
./mvnw jacoco:report

# Check for dependency updates
./mvnw versions:display-dependency-updates
```

### 2. Git Hooks for Development

Create `.git/hooks/pre-commit`:
```bash
#!/bin/bash
# Pre-commit hook for code quality

echo "Running pre-commit checks..."

# Check if Maven build passes
./mvnw clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "Build failed. Commit aborted."
    exit 1
fi

# Run tests
./mvnw test
if [ $? -ne 0 ]; then
    echo "Tests failed. Commit aborted."
    exit 1
fi

# Check code formatting (if configured)
./mvnw fmt:check 2>/dev/null
if [ $? -ne 0 ]; then
    echo "Code formatting issues found. Run: ./mvnw fmt:format"
    exit 1
fi

echo "Pre-commit checks passed!"
```

### 3. Development Utility Scripts

Create `dev-scripts/dev-server.sh`:
```bash
#!/bin/bash
# Development server startup script

echo "Starting Time Traceability Backend Development Server..."

# Set environment variables
export SPRING_PROFILES_ACTIVE=dev
export DB_PASSWORD=dev_password
export DATA_FOLDER=$(pwd)/dev-data/satellite_data

# Create directories if they don't exist
mkdir -p dev-data/satellite_data/{IRLMF,IRXYZ,IRABC}
mkdir -p dev-data/logs

# Start the application with hot reload
./mvnw spring-boot:run -Dspring.profiles.active=dev \
    -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
```

## 🧪 Testing Environment

### 1. Test Configuration
Create `src/test/resources/application-test.properties`:

```properties
# Test Database (H2 in-memory)
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop

# Test Configuration
irnss.parent-folder=src/test/resources/test-data
irnss.scheduler.enabled=false
app.scheduler.materialized-view.enabled=false

# Test Logging
logging.level.com.time.tracealibility=DEBUG
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
```

### 2. Test Data Setup
```bash
# Create test data directory structure
mkdir -p src/test/resources/test-data/{IRLMF,IRXYZ,IRABC}

# Create test files
cat > src/test/resources/test-data/IRLMF/GZLI2P60.866 << 'EOF'
1 123456.789 123456.790 123456.791
2 123457.789 123457.790 123457.791
EOF

cat > src/test/resources/test-data/IRXYZ/GZXY3P60.866 << 'EOF'
1 234567.789 234567.790 234567.791
2 234568.789 234568.790 234568.791
EOF
```

### 3. Integration Test with Testcontainers

Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

Create integration test base class:
```java
@SpringBootTest
@Testcontainers
public abstract class IntegrationTestBase {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_password");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
```

## 🐛 Debug Configuration

### 1. Remote Debugging Setup
```bash
# Start application with remote debugging
./mvnw spring-boot:run -Dspring.profiles.active=dev \
    -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"

# Or with Maven directly
mvn spring-boot:run -Dspring.profiles.active=dev \
    -Drun.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005"
```

### 2. IDE Debug Configuration

#### IntelliJ IDEA Remote Debug
```
Run → Edit Configurations → Add New → Remote JVM Debug

Configuration:
- Name: Time Traceability Remote Debug
- Transport: Socket
- Debugger mode: Attach to remote JVM
- Host: localhost
- Port: 5005
- Command line arguments for remote JVM: 
  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005
```

#### VS Code Debug Configuration
Add to `.vscode/launch.json`:
```json
{
  "type": "java",
  "name": "Remote Debug",
  "request": "attach",
  "hostName": "localhost",
  "port": 5005,
  "projectName": "tracealibility"
}
```

### 3. Debug Logging Configuration
Create `src/main/resources/logback-spring.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>dev-data/logs/application.log</file>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>dev-data/logs/application.%d{yyyy-MM-dd}.%i.log</fileNamePattern>
                <maxFileSize>10MB</maxFileSize>
                <maxHistory>30</maxHistory>
            </rollingPolicy>
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        
        <!-- Application loggers -->
        <logger name="com.time.tracealibility" level="DEBUG"/>
        <logger name="org.springframework.web" level="DEBUG"/>
        <logger name="org.hibernate.SQL" level="DEBUG"/>
        <logger name="org.hibernate.type.descriptor.sql.BasicBinder" level="TRACE"/>
        
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

## 🔥 Hot Reload Setup

### 1. Spring Boot DevTools Configuration
Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

### 2. IDE-Specific Hot Reload

#### IntelliJ IDEA
```
File → Settings → Build, Execution, Deployment → Compiler
- Build project automatically: ✓

Registry (Ctrl+Shift+Alt+/):
- compiler.automake.allow.when.app.running: ✓
```

#### VS Code
Install and configure:
- Java Extension Pack
- Spring Boot Extension Pack

Auto-build is enabled by default.

### 3. Custom Hot Reload Configuration
In `application-dev.properties`:
```properties
# DevTools Configuration
spring.devtools.restart.enabled=true
spring.devtools.restart.additional-paths=src/main/java,src/main/resources
spring.devtools.restart.exclude=static/**,public/**,dev-data/**
spring.devtools.livereload.enabled=true
spring.devtools.livereload.port=35729

# Fast restart for development
spring.devtools.restart.poll-interval=1000
spring.devtools.restart.quiet-period=400
```

## 📋 Development Workflows

### 1. Daily Development Workflow
```bash
# 1. Start development session
cd ~/dev/time-traceability/time_traceability_backend
git pull origin main

# 2. Create feature branch
git checkout -b feature/new-feature-name

# 3. Start development environment
./dev-scripts/dev-server.sh

# 4. Make changes and test
# Edit code in IDE
# Automatic hot reload will restart the application

# 5. Run tests
./mvnw test

# 6. Commit changes
git add .
git commit -m "feat: add new feature description"

# 7. Push to remote
git push origin feature/new-feature-name
```

### 2. Testing Workflow
```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=IrnssDataServiceTest

# Run tests with coverage
./mvnw test jacoco:report

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Run tests in debug mode
./mvnw test -Dmaven.surefire.debug
```

### 3. Database Development Workflow
```bash
# Reset development database
./dev-scripts/db-reset.sh

# Load sample data
psql -U dev_user -d time_traceability_dev -f dev-scripts/db-sample-data.sql

# Watch database changes
./mvnw spring-boot:run -Dspring.profiles.active=dev -Dspring.jpa.show-sql=true

# Backup development data
pg_dump -U dev_user time_traceability_dev > dev-data/backup/dev_backup_$(date +%Y%m%d).sql
```

## ❗ Common Development Issues

### 1. Port Already in Use
```bash
# Find process using port 6003
lsof -i :6003
netstat -tlnp | grep :6003

# Kill process
kill -9 <PID>

# Or change port in application-dev.properties
server.port=6004
```

### 2. Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Test connection
psql -U dev_user -d time_traceability_dev -h localhost

# Reset password
sudo -u postgres psql
ALTER USER dev_user PASSWORD 'new_password';
```

### 3. Maven Build Issues
```bash
# Clean and rebuild
./mvnw clean install -DskipTests

# Clear Maven cache
rm -rf ~/.m2/repository
./mvnw clean install

# Check for conflicts
./mvnw dependency:tree
```

### 4. Hot Reload Not Working
```bash
# Check DevTools dependency
./mvnw dependency:tree | grep devtools

# Restart IDE and application
# Check IDE auto-build settings
# Verify exclude patterns in application-dev.properties
```

### 5. Memory Issues During Development
```bash
# Increase JVM memory
export MAVEN_OPTS="-Xmx2g -Xms1g"

# Or in IDE run configuration:
-Xmx2g -Xms1g -XX:+UseG1GC

# Monitor memory usage
jstat -gc <PID>
```

---

## 📞 Development Support

For development environment issues:

1. **IDE Problems**: Check IDE-specific configuration sections
2. **Build Issues**: Review Maven configuration and dependencies
3. **Database Issues**: Verify PostgreSQL setup and connection
4. **Hot Reload Issues**: Check DevTools configuration and IDE settings

**Related Documentation**: 
- [Configuration Guide](Configuration-Guide.md) for configuration details
- [Installation Guide](Installation-Guide.md) for initial setup
- [Quick Start Guide](Quick-Start-Guide.md) for getting started

---

**Development Environment Ready!** 🚀 You're all set for efficient Time Traceability Backend development.
