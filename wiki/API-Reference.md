# 📊 API Reference

Complete REST API documentation for the Time Traceability Backend service.

## 🌐 Base Configuration

- **Base URL**: `http://localhost:6003/time-traceability-service`
- **Content-Type**: `application/json`
- **API Version**: `v1`
- **Response Format**: JSON

## 📋 Endpoints Overview

| Category | Endpoint | Purpose |
|----------|----------|---------|
| **Data Query** | `/api/data/*` | Satellite data retrieval and querying |
| **Statistics** | `/api/statistics/*` | Analytics and reporting |
| **System** | `/api/scheduler/*` | System management and health |

## 🔍 Data Query APIs

### Get Bulk Location Data
Retrieve all data for specific locations with optional date filtering.

```http
GET /api/data/bulk-location-data
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `source2` | Array[String] | Yes | List of source locations (e.g., IRLMF,IRXYZ) |
| `startDate` | String | No | Start date (YYYY-MM-DD format) |
| `endDate` | String | No | End date (YYYY-MM-DD format) |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/data/bulk-location-data?source2=IRLMF,IRXYZ&startDate=2025-01-01&endDate=2025-12-31"
```

**Example Response:**
```json
{
  "data": [
    {
      "id": "12345",
      "source": "IRLMF",
      "mjd": 60866,
      "sessionNumber": 1,
      "mjdDateTime": "2025-09-08T12:00:00",
      "satATime": 123456.789,
      "satBTime": 123456.790,
      "timeDifference": 0.001
    }
  ],
  "totalElements": 1,
  "cached": true
}
```

### Get Optimized Satellite Differences
Paginated satellite difference data with advanced filtering and sorting.

```http
GET /api/data/optimized-sat-differences
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number (0-based) |
| `size` | Integer | No | 20 | Number of records per page |
| `sortBy` | String | No | mjdDateTime | Sort field |
| `sortDirection` | String | No | desc | Sort direction (asc/desc) |
| `startDate` | String | No | - | Filter start date |
| `endDate` | String | No | - | Filter end date |
| `satLetter` | String | No | - | Satellite identifier |
| `source2` | Array[String] | Yes | - | Source locations |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/data/optimized-sat-differences?page=0&size=20&sortBy=mjdDateTime&sortDirection=desc&source2=IRLMF"
```

**Example Response:**
```json
{
  "content": [
    {
      "id": "combo_001",
      "source": "IRLMF",
      "mjd": 60866,
      "sessionNumber": 1,
      "mjdDateTime": "2025-09-08T12:00:00",
      "satATime": 123456.789,
      "satBTime": 123456.790,
      "timeDifference": 0.001,
      "qualityFlag": "GOOD"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "ascending": false
    }
  },
  "totalElements": 150,
  "totalPages": 8,
  "first": true,
  "last": false
}
```

### Get Session Completeness
Analyze session completeness for specific sources and dates.

```http
GET /api/data/session-completeness
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `source` | String | No | Source location filter |
| `mjd` | String | No | Modified Julian Date filter |
| `currentSessionCount` | Integer | No | Current session count filter |
| `expectedSessionCount` | Integer | No | Expected session count filter |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/data/session-completeness?source=IRLMF&mjd=60866"
```

**Example Response:**
```json
[
  {
    "source": "IRLMF",
    "mjd": 60866,
    "currentSessionCount": 24,
    "expectedSessionCount": 24,
    "completenessPercentage": 100.0,
    "missingSessionNumbers": [],
    "status": "COMPLETE"
  }
]
```

### Get Available MJDs
Retrieve list of available Modified Julian Days.

```http
GET /api/data/available-mjds
```

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/data/available-mjds"
```

**Example Response:**
```json
["60860", "60861", "60862", "60863", "60864", "60865", "60866"]
```

### Get Pivoted Satellite Data
Retrieve satellite data in pivoted format for analysis.

```http
GET /api/data/pivoted-sat-data
```

**Parameters:**
| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `page` | Integer | No | 0 | Page number |
| `size` | Integer | No | 15 | Page size |
| `sortBy` | String | No | mjd_date_time | Sort field |
| `sortDirection` | String | No | desc | Sort direction |
| `startDate` | String | No | - | Start date filter |
| `endDate` | String | No | - | End date filter |
| `satLetter` | String | No | - | Satellite filter |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/data/pivoted-sat-data?page=0&size=15&satLetter=A"
```

**Example Response:**
```json
{
  "content": [
    {
      "id": "pivot_001",
      "mjdDateTime": "2025-09-08T12:00:00",
      "satA": 123456.789,
      "satB": 123456.790,
      "satC": 123456.791,
      "source": "IRLMF",
      "sessionNumber": 1
    }
  ],
  "totalElements": 75,
  "totalPages": 5
}
```

## 📊 Statistics APIs

### Get File Upload Statistics
Comprehensive file upload and processing statistics.

```http
GET /api/statistics/file-upload-stats
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | DateTime | No | Start date (ISO format) |
| `endDate` | DateTime | No | End date (ISO format) |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats?startDate=2025-01-01T00:00:00&endDate=2025-12-31T23:59:59"
```

**Example Response:**
```json
{
  "summaryStats": {
    "totalFilesProcessed": 1500,
    "totalDataPoints": 45000,
    "averageProcessingTimeMs": 250,
    "successRate": 98.5,
    "dateRange": {
      "startDate": "2025-01-01T00:00:00",
      "endDate": "2025-12-31T23:59:59"
    }
  },
  "dailyUploadStats": [
    {
      "date": "2025-09-08",
      "filesProcessed": 24,
      "dataPointsProcessed": 720,
      "averageProcessingTime": 245,
      "errorCount": 0
    }
  ],
  "locationUploadStats": [
    {
      "location": "IRLMF",
      "totalFiles": 500,
      "successfulFiles": 495,
      "failedFiles": 5,
      "averageFilesPerDay": 20.8,
      "lastUploadTime": "2025-09-08T12:00:00"
    }
  ],
  "fileTypeStats": [
    {
      "fileExtension": ".24d",
      "totalFiles": 750,
      "averageFileSize": 1024000,
      "processingSuccessRate": 99.2
    }
  ],
  "processingPerformanceStats": {
    "averageProcessingTimeMs": 250,
    "medianProcessingTimeMs": 200,
    "maxProcessingTimeMs": 2500,
    "minProcessingTimeMs": 50,
    "totalProcessingTimeHours": 12.5
  },
  "recentFileUploads": [
    {
      "fileName": "GZLI2P60.866",
      "location": "IRLMF",
      "processedAt": "2025-09-08T12:00:00",
      "processingTimeMs": 200,
      "dataPointsExtracted": 30,
      "status": "SUCCESS"
    }
  ],
  "hourlyUploadPattern": [
    {
      "hour": 0,
      "averageFilesProcessed": 2.5,
      "peakProcessingTime": "00:15:00"
    }
  ],
  "missingFileInfo": [
    {
      "expectedFileName": "GZLI2P60.867",
      "location": "IRLMF",
      "expectedDate": "2025-09-09",
      "daysMissing": 1
    }
  ],
  "filesWithErrors": [
    {
      "fileName": "CORRUPTED_FILE.24d",
      "location": "IRXYZ",
      "errorMessage": "Invalid character encoding",
      "errorTime": "2025-09-08T10:30:00",
      "retryCount": 3
    }
  ]
}
```

### Get Location-Specific Statistics
Statistics for a specific monitoring location.

```http
GET /api/statistics/file-upload-stats/location/{locationName}
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `locationName` | String | Yes | Location identifier (e.g., IRLMF) |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats/location/IRLMF"
```

### Get MJD-Specific Statistics
Statistics for a specific Modified Julian Date.

```http
GET /api/statistics/file-upload-stats/mjd/{mjd}
```

**Parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `mjd` | Integer | Yes | Modified Julian Date |

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats/mjd/60866"
```

## 🔧 System Management APIs

### Get System Health
Check overall system health and status.

```http
GET /api/scheduler/health
```

**Example Request:**
```bash
curl "http://localhost:6003/time-traceability-service/api/scheduler/health"
```

**Example Response:**
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "connectionPool": "HEALTHY",
        "responseTime": "5ms"
      }
    },
    "fileSystem": {
      "status": "UP",
      "details": {
        "dataDirectory": "ACCESSIBLE",
        "freeSpace": "150GB"
      }
    },
    "materializedViews": {
      "status": "UP",
      "details": {
        "lastRefresh": "2025-09-08T12:00:00",
        "nextRefresh": "2025-09-08T12:05:00"
      }
    }
  }
}
```

### Get Scheduler Status
Check materialized view refresh status.

```http
GET /api/scheduler/status
```

**Example Response:**
```json
{
  "schedulerEnabled": true,
  "lastRefreshTime": "2025-09-08T12:00:00",
  "nextRefreshTime": "2025-09-08T12:05:00",
  "refreshInterval": "5 minutes",
  "materializedViews": [
    {
      "name": "sat_common_view_difference_materialized",
      "status": "UP_TO_DATE",
      "lastRefresh": "2025-09-08T12:00:00",
      "rowCount": 15000
    }
  ]
}
```

### Refresh Materialized Views
Manually trigger materialized view refresh.

```http
POST /api/scheduler/refresh
```

**Example Request:**
```bash
curl -X POST "http://localhost:6003/time-traceability-service/api/scheduler/refresh"
```

**Example Response:**
```json
{
  "message": "Materialized view refresh initiated",
  "refreshId": "refresh_20250908_120000",
  "estimatedCompletionTime": "2025-09-08T12:02:00"
}
```

## ❌ Error Responses

### Standard Error Format
```json
{
  "timestamp": "2025-09-08T12:00:00.000+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid parameter value",
  "path": "/api/data/bulk-location-data",
  "details": {
    "field": "source2",
    "rejectedValue": "INVALID_SOURCE",
    "message": "Source must be a valid location identifier"
  }
}
```

### Common Error Codes

| Status Code | Error Type | Description |
|-------------|------------|-------------|
| 400 | Bad Request | Invalid parameters or request format |
| 404 | Not Found | Resource not found |
| 500 | Internal Server Error | Server processing error |
| 503 | Service Unavailable | System maintenance or overload |

### Error Examples

#### 400 - Invalid Date Format
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid date format. Expected: YYYY-MM-DD",
  "path": "/api/data/bulk-location-data"
}
```

#### 404 - Location Not Found
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Location 'INVALID' not found in system",
  "path": "/api/statistics/file-upload-stats/location/INVALID"
}
```

#### 503 - Database Unavailable
```json
{
  "status": 503,
  "error": "Service Unavailable",
  "message": "Database is temporarily unavailable",
  "path": "/api/data/optimized-sat-differences"
}
```

## 🔐 Authentication

Currently, the API operates without authentication for internal use. Future versions may include:

- **API Key Authentication**: For external integrations
- **JWT Tokens**: For session-based access
- **Role-Based Access**: For different user permissions

## 📊 Rate Limiting

Current rate limiting policies:

- **Bulk Data Endpoints**: 10 requests per minute
- **Statistics Endpoints**: 60 requests per minute
- **System Endpoints**: 30 requests per minute

## 🧪 Testing Examples

### Using cURL
```bash
# Test basic connectivity
curl -v "http://localhost:6003/time-traceability-service/api/scheduler/health"

# Get statistics with date range
curl -G "http://localhost:6003/time-traceability-service/api/statistics/file-upload-stats" \
  --data-urlencode "startDate=2025-01-01T00:00:00" \
  --data-urlencode "endDate=2025-12-31T23:59:59"

# Get paginated data
curl -G "http://localhost:6003/time-traceability-service/api/data/optimized-sat-differences" \
  --data-urlencode "source2=IRLMF,IRXYZ" \
  --data-urlencode "page=0" \
  --data-urlencode "size=20"
```

### Using Postman
1. Import the [Postman Collection](../postman/Time-Traceability-API.postman_collection.json)
2. Set environment variable `baseUrl` to `http://localhost:6003/time-traceability-service`
3. Run the collection tests

---

This API reference provides comprehensive documentation for integrating with the Time Traceability Backend. For more advanced usage patterns, see the [Development Workflow](Development-Workflow.md) guide.
