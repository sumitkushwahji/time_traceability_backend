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

### Testing Your Setup
```bash
# Check system health
curl http://localhost:6003/time-traceability-service/api/scheduler/health

# Get available MJDs
curl http://localhost:6003/time-traceability-service/api/data/available-mjds

# Check statistics
curl http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats
```

## ⚠️ Common Issues

### Database Connection Failed
- Check PostgreSQL is running: `pg_isready -h localhost -p 5432`
- Verify credentials with psql

### Port Already in Use
- Check what's using port 6003: `netstat -tulpn | grep 6003`
- Kill the process or change port in application.properties

### File Processing Not Working
- ✅ Check if the `irnss.parent-folder` path exists
- ✅ Verify file permissions (read access required)
- ✅ Check application logs for specific errors

## 📝 Next Steps

After successful installation:

1. 📖 Read the [System Architecture](System-Architecture) guide
2. 🔧 Review configuration guide for advanced settings
3. 👨‍💻 Check development workflow if you're contributing
4. 📊 Explore [API Reference](API-Reference) for available endpoints

---

🎉 **Congratulations!** You now have Time Traceability Backend running locally. Start exploring the APIs and processing satellite data!
