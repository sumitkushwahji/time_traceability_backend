# 🚀 Quick Start Guide

Get the Time Traceability Backend up and running in 5 minutes!

## ⚡ Prerequisites

Before you begin, ensure you have:

- ☑️ **Java 17 or higher** installed
- ☑️ **PostgreSQL 12+** running
- ☑️ **Maven 3.6+** installed
- ☑️ **Git** for version control

## 🛠️ 5-Minute Setup

### Step 1: Clone the Repository
```bash
git clone https://github.com/sumitkushwahji/time_traceability_backend.git
cd time_traceability_backend
```

### Step 2: Set Up Database
```sql
-- Connect to PostgreSQL as superuser
CREATE DATABASE time_traceability;
CREATE USER traceability_user WITH PASSWORD 'your_secure_password';
GRANT ALL PRIVILEGES ON DATABASE time_traceability TO traceability_user;
```

### Step 3: Configure Application
Create or edit `src/main/resources/application-dev.properties`:
```properties
# Database Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/time_traceability
spring.datasource.username=traceability_user
spring.datasource.password=your_secure_password

# File Processing (adjust path to your data directory)
irnss.parent-folder=C:/satellite_data

# Server Configuration
server.port=6003
```

### Step 4: Build and Run
```bash
# Clean build
mvn clean install

# Run in development mode
mvn spring-boot:run -Dspring.profiles.active=dev
```

### Step 5: Verify Installation
Open your browser and check:
- 🌐 **Health Check**: http://localhost:6003/time-traceability-service/api/scheduler/health
- 📊 **Statistics**: http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats

## 🎯 What's Next?

### Immediate Tasks
1. **Set up your data directory**: Create the folder structure for satellite files
2. **Review configuration**: Check all settings in application properties
3. **Test API endpoints**: Use Postman or curl to test the APIs
4. **Set up monitoring**: Configure logging and monitoring

### Directory Structure Setup
```bash
# Create satellite data directories
mkdir -p /path/to/satellite_data/IRLMF
mkdir -p /path/to/satellite_data/IRXYZ
mkdir -p /path/to/satellite_data/IRABC

# Copy sample files (if available)
cp sample_files/* /path/to/satellite_data/IRLMF/
```

### Testing Your Setup
```bash
# Check system health
curl http://localhost:6003/time-traceability-service/api/scheduler/health

# Get available MJDs
curl http://localhost:6003/time-traceability-service/api/data/available-mjds

# Check statistics
curl http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats
```

## 🐳 Docker Quick Start

If you prefer Docker:

```bash
# Build image
docker build -t time-traceability-backend .

# Run with environment variables
docker run -d \
  --name time-traceability \
  -p 6003:6003 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/time_traceability \
  -e SPRING_DATASOURCE_USERNAME=traceability_user \
  -e SPRING_DATASOURCE_PASSWORD=your_secure_password \
  -v /path/to/satellite_data:/app/data \
  time-traceability-backend
```

## ⚠️ Common Issues

### Database Connection Failed
```bash
# Check PostgreSQL is running
pg_isready -h localhost -p 5432

# Verify credentials
psql -h localhost -U traceability_user -d time_traceability
```

### Port Already in Use
```bash
# Check what's using port 6003
netstat -tulpn | grep 6003

# Kill the process or change port in application.properties
server.port=6004
```

### File Processing Not Working
- ✅ Check if the `irnss.parent-folder` path exists
- ✅ Verify file permissions (read access required)
- ✅ Check application logs for specific errors

## 📝 Next Steps

After successful installation:

1. 📖 Read the [System Architecture](System-Architecture.md) guide
2. 🔧 Review [Configuration Guide](Configuration-Guide.md) for advanced settings
3. 👨‍💻 Check [Development Workflow](Development-Workflow.md) if you're contributing
4. 📊 Explore [API Reference](API-Reference.md) for available endpoints

## 🆘 Getting Help

- **Installation Issues**: Check [Troubleshooting](Troubleshooting.md)
- **Configuration Problems**: See [Configuration Reference](Configuration-Reference.md)
- **API Questions**: Refer to [API Reference](API-Reference.md)
- **Performance Issues**: Check [Performance Optimization](Performance-Optimization.md)

---

🎉 **Congratulations!** You now have Time Traceability Backend running locally. Start exploring the APIs and processing satellite data!

**Need more detailed setup?** Check the [Installation Guide](Installation-Guide.md) for comprehensive instructions.
