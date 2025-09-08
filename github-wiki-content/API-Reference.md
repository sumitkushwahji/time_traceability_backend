# 📊 API Reference

Complete REST API documentation for the Time Traceability Backend service.

## 🌐 Base Configuration

- **Base URL**: `http://localhost:6003/time-traceability-service`
- **Content-Type**: `application/json`
- **Response Format**: JSON

## 📋 Endpoints Overview

| Category | Endpoint | Purpose |
|----------|----------|---------|
| **Data Query** | `/api/data/*` | Satellite data retrieval and querying |
| **Statistics** | `/api/statistics/*` | Analytics and reporting |
| **System** | `/api/scheduler/*` | System management and health |

## 🔍 Data Query APIs

### Get Bulk Location Data
```http
GET /api/data/bulk-location-data?source2=IRLMF,IRXYZ&startDate=2025-01-01&endDate=2025-12-31
```

**Parameters:**
- `source2` (required): List of source locations
- `startDate` (optional): Start date (YYYY-MM-DD format)
- `endDate` (optional): End date (YYYY-MM-DD format)

### Get Optimized Satellite Differences
```http
GET /api/data/optimized-sat-differences?page=0&size=20&source2=IRLMF
```

**Parameters:**
- `page` (default: 0): Page number
- `size` (default: 20): Records per page
- `sortBy` (default: mjdDateTime): Sort field
- `sortDirection` (default: desc): Sort direction
- `source2` (required): Source locations

### Get Available MJDs
```http
GET /api/data/available-mjds
```

Returns list of available Modified Julian Days.

### Get Session Completeness
```http
GET /api/data/session-completeness?source=IRLMF&mjd=60866
```

Analyze session completeness for specific sources and dates.

## 📊 Statistics APIs

### Get File Upload Statistics
```http
GET /api/statistics/file-upload-stats?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59
```

**Response includes:**
- Summary statistics
- Daily upload patterns
- Location-specific data
- Processing performance metrics
- Recent file uploads
- Error information

### Get Location Statistics
```http
GET /api/statistics/file-upload-stats/location/{locationName}
```

### Get MJD Statistics
```http
GET /api/statistics/file-upload-stats/mjd/{mjd}
```

## 🔧 System Management APIs

### Get System Health
```http
GET /api/scheduler/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "database": {"status": "UP"},
    "fileSystem": {"status": "UP"},
    "materializedViews": {"status": "UP"}
  }
}
```

### Get Scheduler Status
```http
GET /api/scheduler/status
```

### Refresh Materialized Views
```http
POST /api/scheduler/refresh
```

## ❌ Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-09-08T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid parameter value",
  "path": "/api/data/bulk-location-data"
}
```

### Common Error Codes
- **400**: Bad Request - Invalid parameters
- **404**: Not Found - Resource not found
- **500**: Internal Server Error - Server processing error
- **503**: Service Unavailable - System maintenance

## 🧪 Testing Examples

### Using cURL
```bash
# Test connectivity
curl -v "http://localhost:6003/time-traceability-service/api/scheduler/health"

# Get statistics
curl -G "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats" \
  --data-urlencode "startDate=2025-01-01T00:00:00"

# Get paginated data
curl -G "http://localhost:6003/time-traceability-service/api/data/optimized-sat-differences" \
  --data-urlencode "source2=IRLMF" \
  --data-urlencode "page=0"
```

---

For detailed API documentation with request/response examples, see the complete API reference in the project documentation.
