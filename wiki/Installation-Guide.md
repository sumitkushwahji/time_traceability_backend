# 📦 Installation Guide

Comprehensive installation guide for the Time Traceability Backend system covering development, testing, and production environments.

## 📋 Table of Contents

- [System Requirements](#-system-requirements)
- [Prerequisites Installation](#-prerequisites-installation)
- [Database Setup](#-database-setup)
- [Application Installation](#-application-installation)
- [Configuration](#-configuration)
- [Environment-Specific Setup](#-environment-specific-setup)
- [Docker Installation](#-docker-installation)
- [Verification](#-verification)
- [Post-Installation Tasks](#-post-installation-tasks)
- [Troubleshooting Installation](#-troubleshooting-installation)

## 🖥️ System Requirements

### Minimum Requirements
- **OS**: Windows 10+, Linux (Ubuntu 18.04+), macOS 10.15+
- **CPU**: 2 cores, 2.0 GHz
- **RAM**: 4 GB (2 GB for JVM)
- **Storage**: 20 GB free space
- **Network**: Internet connection for dependencies

### Recommended Requirements
- **OS**: Linux (Ubuntu 20.04+ LTS) or Windows Server 2019+
- **CPU**: 4+ cores, 3.0 GHz
- **RAM**: 8+ GB (4 GB for JVM)
- **Storage**: 100+ GB SSD
- **Network**: High-speed connection

### Production Requirements
- **OS**: Linux (RHEL 8+, Ubuntu 20.04+ LTS)
- **CPU**: 8+ cores, 3.2 GHz
- **RAM**: 16+ GB (8 GB for JVM)
- **Storage**: 500+ GB SSD with backup
- **Network**: Dedicated network with monitoring

## 🛠️ Prerequisites Installation

### 1. Java Development Kit (JDK) 17+

#### On Windows:
```bash
# Download and install from Oracle or use Chocolatey
choco install openjdk17

# Verify installation
java -version
javac -version
```

#### On Linux (Ubuntu/Debian):
```bash
# Install OpenJDK 17
sudo apt update
sudo apt install openjdk-17-jdk

# Set JAVA_HOME
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' >> ~/.bashrc
echo 'export PATH=$PATH:$JAVA_HOME/bin' >> ~/.bashrc
source ~/.bashrc

# Verify installation
java -version
```

#### On macOS:
```bash
# Using Homebrew
brew install openjdk@17

# Add to PATH
echo 'export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc

# Verify installation
java -version
```

### 2. Apache Maven 3.6+

#### On Windows:
```bash
# Using Chocolatey
choco install maven

# Manual installation:
# 1. Download from https://maven.apache.org/download.cgi
# 2. Extract to C:\Program Files\Apache\maven
# 3. Add to PATH: C:\Program Files\Apache\maven\bin
```

#### On Linux:
```bash
# Ubuntu/Debian
sudo apt install maven

# CentOS/RHEL
sudo yum install maven

# Verify installation
mvn -version
```

#### On macOS:
```bash
# Using Homebrew
brew install maven

# Verify installation
mvn -version
```

### 3. PostgreSQL 12+

#### On Windows:
```bash
# Download installer from https://www.postgresql.org/download/windows/
# Or use Chocolatey
choco install postgresql

# Start PostgreSQL service
net start postgresql-x64-13
```

#### On Linux (Ubuntu/Debian):
```bash
# Install PostgreSQL
sudo apt update
sudo apt install postgresql postgresql-contrib

# Start and enable PostgreSQL
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Set password for postgres user
sudo -u postgres psql
\password postgres
\q
```

#### On macOS:
```bash
# Using Homebrew
brew install postgresql

# Start PostgreSQL service
brew services start postgresql

# Create postgres user (if needed)
createuser -s postgres
```

### 4. Git (for version control)

#### On Windows:
```bash
# Download from https://git-scm.com/download/win
# Or use Chocolatey
choco install git
```

#### On Linux:
```bash
# Ubuntu/Debian
sudo apt install git

# CentOS/RHEL
sudo yum install git
```

#### On macOS:
```bash
# Using Homebrew
brew install git

# Or use Xcode Command Line Tools
xcode-select --install
```

## 🗄️ Database Setup

### 1. Create Database and User

```sql
-- Connect as postgres superuser
psql -U postgres

-- Create database
CREATE DATABASE time_traceability;

-- Create application user
CREATE USER traceability_user WITH PASSWORD 'secure_password_here';

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE time_traceability TO traceability_user;

-- Connect to the new database
\c time_traceability

-- Grant schema privileges
GRANT ALL ON SCHEMA public TO traceability_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO traceability_user;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO traceability_user;

-- Exit psql
\q
```

### 2. Configure PostgreSQL

#### Edit postgresql.conf:
```bash
# Find configuration file
sudo find /etc -name "postgresql.conf" 2>/dev/null

# Edit configuration (adjust path as needed)
sudo nano /etc/postgresql/13/main/postgresql.conf
```

**Key settings:**
```ini
# Connection settings
listen_addresses = 'localhost'          # or '*' for all addresses
port = 5432
max_connections = 100

# Memory settings
shared_buffers = 256MB                   # 25% of RAM for dedicated server
effective_cache_size = 1GB               # 75% of RAM
work_mem = 4MB
maintenance_work_mem = 64MB

# Write-ahead logging
wal_buffers = 16MB
checkpoint_completion_target = 0.9
```

#### Edit pg_hba.conf:
```bash
sudo nano /etc/postgresql/13/main/pg_hba.conf
```

**Add authentication rules:**
```ini
# Local connections
local   all             postgres                                peer
local   all             traceability_user                       md5

# IPv4 local connections
host    all             all             127.0.0.1/32            md5
host    time_traceability traceability_user 127.0.0.1/32        md5
```

### 3. Restart PostgreSQL
```bash
# Linux
sudo systemctl restart postgresql

# Windows
net stop postgresql-x64-13
net start postgresql-x64-13

# macOS
brew services restart postgresql
```

### 4. Verify Database Setup
```bash
# Test connection
psql -h localhost -U traceability_user -d time_traceability

# Should connect successfully and show:
# time_traceability=>
```

## 📱 Application Installation

### 1. Clone Repository

```bash
# Clone the repository
git clone https://github.com/sumitkushwahji/time_traceability_backend.git

# Navigate to project directory
cd time_traceability_backend

# Check branch (should be on main or specific feature branch)
git branch -a
git checkout main  # or your target branch
```

### 2. Verify Project Structure

```bash
# Verify key files exist
ls -la
# Should see: pom.xml, src/, README.md, etc.

# Check Maven wrapper
ls -la mvnw*
# Should see: mvnw, mvnw.cmd
```

### 3. Install Dependencies

```bash
# Clean and install dependencies
./mvnw clean install

# On Windows
mvnw.cmd clean install

# Skip tests for faster initial build
./mvnw clean install -DskipTests

# Verify successful build
echo $?  # Should output 0 for success
```

### 4. Build Verification

```bash
# Check if JAR file was created
ls -la target/
# Should see: tracealibility-*.jar

# Check target directory size
du -sh target/
# Should be reasonable size (50-200MB)
```

## ⚙️ Configuration

### 1. Create Configuration Files

#### Development Configuration:
```bash
# Create development properties file
cp src/main/resources/application.properties src/main/resources/application-dev.properties
```

**Edit `application-dev.properties`:**
```properties
# Server Configuration
server.port=6003
server.servlet.context-path=/time-traceability-service

# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=traceability_user
spring.datasource.password=secure_password_here
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true

# File Processing Configuration
irnss.parent-folder=/path/to/your/satellite/data
irnss.processing.batch-size=100
irnss.processing.max-retries=3
irnss.processing.parallel-directories=true

# Scheduler Configuration
irnss.scheduler.enabled=true
irnss.scheduler.fixed-rate=300000
irnss.scheduler.initial-delay=60000

# Materialized View Configuration
app.scheduler.materialized-view.enabled=true
app.scheduler.materialized-view.refresh-interval=300000
app.scheduler.materialized-view.names=sat_common_view_difference_materialized,sat_pivoted_view_materialized

# Logging Configuration
logging.level.com.time.tracealibility=INFO
logging.level.org.springframework=WARN
logging.level.org.hibernate=WARN
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.file.name=logs/application.log

# CORS Configuration
app.cors.allowed-origins=http://localhost:4200,http://127.0.0.1:4200
app.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
app.cors.allowed-headers=*
app.cors.allow-credentials=true
```

### 2. Create Data Directories

```bash
# Create satellite data directory structure
mkdir -p /path/to/satellite/data
mkdir -p /path/to/satellite/data/IRLMF
mkdir -p /path/to/satellite/data/IRXYZ
mkdir -p /path/to/satellite/data/IRABC

# Set appropriate permissions
chmod 755 /path/to/satellite/data
chmod 755 /path/to/satellite/data/*

# Create logs directory
mkdir -p logs
chmod 755 logs
```

### 3. Environment Variables (Optional)

```bash
# Create environment file
cat > .env << 'EOF'
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=time_traceability
DB_USER=traceability_user
DB_PASSWORD=secure_password_here

# Application
APP_PORT=6003
APP_PROFILE=dev
DATA_FOLDER=/path/to/satellite/data

# JVM Options
JAVA_OPTS="-Xmx2g -Xms1g -XX:+UseG1GC"
EOF

# Load environment variables
source .env
```

## 🌍 Environment-Specific Setup

### Development Environment

```bash
# Run in development mode
./mvnw spring-boot:run -Dspring.profiles.active=dev

# Or with custom JVM options
./mvnw spring-boot:run -Dspring.profiles.active=dev -Dspring-boot.run.jvmArguments="-Xmx2g -Xms1g"

# Background execution
nohup ./mvnw spring-boot:run -Dspring.profiles.active=dev > logs/dev.log 2>&1 &
```

### Testing Environment

**Create `application-test.properties`:**
```properties
# Use H2 in-memory database for testing
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# JPA settings for testing
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true

# Disable scheduler for testing
irnss.scheduler.enabled=false

# Test data folder
irnss.parent-folder=src/test/resources/test-data
```

### Production Environment

**Create `application-prod.properties`:**
```properties
# Server Configuration
server.port=6003
server.servlet.context-path=/time-traceability-service

# Database Configuration (use environment variables)
spring.datasource.url=jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:time_traceability}
spring.datasource.username=${DB_USER:traceability_user}
spring.datasource.password=${DB_PASSWORD}

# Connection Pool Settings
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=900000

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

# Production File Processing
irnss.parent-folder=${DATA_FOLDER:/opt/satellite/data}
irnss.processing.batch-size=500
irnss.processing.parallel-directories=true

# Logging
logging.level.com.time.tracealibility=INFO
logging.level.org.springframework=WARN
logging.file.name=/var/log/time-traceability/application.log
logging.file.max-size=100MB
logging.file.max-history=30

# Actuator (for monitoring)
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when_authorized
```

## 🐳 Docker Installation

### 1. Create Dockerfile

```dockerfile
FROM openjdk:17-jre-slim

# Set working directory
WORKDIR /app

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Copy JAR file
COPY target/tracealibility-*.jar app.jar

# Create directories
RUN mkdir -p /app/logs /app/data && \
    chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 6003

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:6003/time-traceability-service/api/scheduler/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:13
    environment:
      POSTGRES_DB: time_traceability
      POSTGRES_USER: traceability_user
      POSTGRES_PASSWORD: secure_password_here
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U traceability_user -d time_traceability"]
      interval: 30s
      timeout: 10s
      retries: 5

  app:
    build: .
    ports:
      - "6003:6003"
    environment:
      - SPRING_PROFILES_ACTIVE=docker
      - DB_HOST=postgres
      - DB_PORT=5432
      - DB_NAME=time_traceability
      - DB_USER=traceability_user
      - DB_PASSWORD=secure_password_here
      - DATA_FOLDER=/app/data
    volumes:
      - ./satellite_data:/app/data
      - ./logs:/app/logs
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:6003/time-traceability-service/api/scheduler/health"]
      interval: 30s
      timeout: 10s
      retries: 3

volumes:
  postgres_data:
```

### 3. Build and Run with Docker

```bash
# Build the application
./mvnw clean package -DskipTests

# Build Docker image
docker build -t time-traceability-backend .

# Run with Docker Compose
docker-compose up -d

# Check status
docker-compose ps
docker-compose logs -f app
```

## ✅ Verification

### 1. Application Health Check

```bash
# Wait for application to start (may take 30-60 seconds)
sleep 60

# Check application health
curl http://localhost:6003/time-traceability-service/api/scheduler/health

# Expected response:
{
  "status": "UP",
  "components": {
    "database": {"status": "UP"},
    "fileSystem": {"status": "UP"}
  }
}
```

### 2. Database Verification

```bash
# Check database tables
psql -h localhost -U traceability_user -d time_traceability -c "
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public';"

# Should show tables like:
# irnss_data
# processed_file
# file_availability
```

### 3. API Endpoints Test

```bash
# Test data endpoints
curl http://localhost:6003/time-traceability-service/api/data/available-mjds

# Test statistics endpoint
curl http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats

# Test scheduler endpoint
curl http://localhost:6003/time-traceability-service/api/scheduler/status
```

### 4. File Processing Test

```bash
# Create test file (if data directory exists)
echo "1 123456.789 123456.790 123456.791" > /path/to/satellite/data/IRLMF/GZLI2P60.866

# Wait for processing (check logs)
tail -f logs/application.log

# Check if file was processed
psql -h localhost -U traceability_user -d time_traceability -c "
SELECT * FROM processed_file LIMIT 5;"
```

## 📋 Post-Installation Tasks

### 1. Service Configuration (Linux)

```bash
# Create systemd service file
sudo cat > /etc/systemd/system/time-traceability-backend.service << 'EOF'
[Unit]
Description=Time Traceability Backend
After=network.target postgresql.service

[Service]
Type=simple
User=appuser
Group=appuser
WorkingDirectory=/opt/time-traceability-backend
ExecStart=/usr/bin/java -jar tracealibility-*.jar
Environment=SPRING_PROFILES_ACTIVE=prod
Environment=JAVA_OPTS="-Xmx4g -Xms2g"
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start service
sudo systemctl daemon-reload
sudo systemctl enable time-traceability-backend
sudo systemctl start time-traceability-backend
sudo systemctl status time-traceability-backend
```

### 2. Log Rotation

```bash
# Create logrotate configuration
sudo cat > /etc/logrotate.d/time-traceability-backend << 'EOF'
/var/log/time-traceability/application.log {
    daily
    rotate 30
    compress
    delaycompress
    missingok
    notifempty
    create 644 appuser appuser
    postrotate
        systemctl reload time-traceability-backend
    endscript
}
EOF
```

### 3. Firewall Configuration

```bash
# Ubuntu/Debian (ufw)
sudo ufw allow 6003/tcp

# CentOS/RHEL (firewalld)
sudo firewall-cmd --permanent --add-port=6003/tcp
sudo firewall-cmd --reload
```

### 4. Backup Setup

```bash
# Create backup script
cat > /opt/scripts/backup-time-traceability.sh << 'EOF'
#!/bin/bash
BACKUP_DIR="/opt/backups/time-traceability"
DATE=$(date +%Y%m%d_%H%M%S)

# Create backup directory
mkdir -p $BACKUP_DIR

# Database backup
pg_dump -h localhost -U traceability_user time_traceability > $BACKUP_DIR/db_backup_$DATE.sql

# Application files backup
tar -czf $BACKUP_DIR/app_backup_$DATE.tar.gz /opt/time-traceability-backend

# Clean old backups (keep 30 days)
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
find $BACKUP_DIR -name "*.tar.gz" -mtime +30 -delete
EOF

# Make executable
chmod +x /opt/scripts/backup-time-traceability.sh

# Add to crontab for daily backup
echo "0 2 * * * /opt/scripts/backup-time-traceability.sh" | sudo crontab -
```

## 🔧 Troubleshooting Installation

### Common Installation Issues

#### Java Version Conflicts
```bash
# Check multiple Java versions
update-alternatives --list java

# Set correct version
sudo update-alternatives --config java
```

#### Maven Build Failures
```bash
# Clear Maven cache
rm -rf ~/.m2/repository

# Rebuild with verbose output
./mvnw clean install -X

# Check for network issues
./mvnw dependency:resolve
```

#### Database Connection Issues
```bash
# Check PostgreSQL status
sudo systemctl status postgresql

# Check if port is open
netstat -tlnp | grep :5432

# Test connection
telnet localhost 5432
```

#### Port Conflicts
```bash
# Find process using port 6003
sudo netstat -tlnp | grep :6003
sudo lsof -i :6003

# Kill conflicting process
sudo kill -9 <PID>
```

### Performance Issues

#### Slow Application Startup
- Increase JVM heap size: `-Xmx4g -Xms2g`
- Disable unnecessary auto-configurations
- Check database connection pool settings

#### High Memory Usage
- Monitor with: `jstat -gc <PID>`
- Tune garbage collector: `-XX:+UseG1GC`
- Adjust connection pool sizes

### Recovery Procedures

#### Corrupted Installation
```bash
# Clean rebuild
rm -rf target/
./mvnw clean
./mvnw install

# Reset database
dropdb -U postgres time_traceability
createdb -U postgres time_traceability
# Re-run database setup
```

#### Configuration Issues
```bash
# Backup current config
cp src/main/resources/application-*.properties backup/

# Reset to defaults
git checkout src/main/resources/application.properties

# Reconfigure step by step
```

---

## 📞 Installation Support

If you encounter issues during installation:

1. **Check Prerequisites**: Verify all software versions meet requirements
2. **Review Logs**: Check application and database logs for specific errors
3. **Consult Troubleshooting**: See [Troubleshooting Guide](Troubleshooting.md)
4. **Contact Support**: Reach out to the development team

**Next Steps**: After successful installation, review the [Configuration Guide](Configuration-Guide.md) for advanced settings and the [Quick Start Guide](Quick-Start-Guide.md) for basic usage.

---

**Installation Complete!** 🎉 Your Time Traceability Backend is now ready for satellite data processing.
