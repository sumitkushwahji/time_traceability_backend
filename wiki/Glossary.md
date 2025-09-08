# 🗃️ Glossary

Technical terms and definitions used throughout the Time Traceability Backend documentation and codebase.

## 📊 Core Concepts

### **API (Application Programming Interface)**
A set of protocols and tools for building software applications. The Time Traceability Backend provides REST APIs for accessing satellite data and analytics.

### **File Availability**
A tracking mechanism that monitors which satellite data files are present and accessible for each source location and date combination.

### **IRNSS (Indian Regional Navigation Satellite System)**
India's regional satellite navigation system providing accurate positioning services. Also known as NavIC (Navigation with Indian Constellation).

### **Materialized View**
A database view that stores the result of a query physically and can be refreshed periodically. Used for performance optimization of complex analytical queries.

### **MJD (Modified Julian Date)**
A continuous count of days from November 17, 1858. Used in astronomy and satellite systems for precise time calculations. Formula: MJD = JD - 2400000.5

### **Real-time Processing**
The capability to process data as it becomes available, typically within seconds or minutes of data arrival.

### **Session**
A time period during which satellite data is collected, typically representing hourly or sub-hourly intervals within a day.

### **Source**
A monitoring station or location identifier, typically the first 6 characters of the satellite data filename (e.g., IRLMF, IRXYZ).

## 🔧 Technical Terms

### **Batch Processing**
Processing multiple records together as a group to improve performance and reduce database overhead.

### **Character Encoding**
A system for representing text characters as binary data. The system supports UTF-8 and ISO-8859-1 encodings.

### **Connection Pooling**
A technique for managing database connections efficiently by reusing existing connections rather than creating new ones for each request.

### **CORS (Cross-Origin Resource Sharing)**
A security feature that allows web applications from one domain to access resources from another domain.

### **DTO (Data Transfer Object)**
A design pattern used to transfer data between different layers or components of an application.

### **Health Check**
An endpoint or mechanism that provides information about the system's operational status and component health.

### **Incremental Processing**
A processing strategy that handles only new or changed data since the last processing run, improving efficiency for large datasets.

### **JPA (Java Persistence API)**
A Java specification for managing relational data, providing object-relational mapping capabilities.

### **Repository Pattern**
A design pattern that encapsulates data access logic and provides a uniform interface for accessing different data sources.

### **REST (Representational State Transfer)**
An architectural style for designing web services that use HTTP methods and status codes for communication.

### **Spring Boot**
A Java framework that simplifies the development of production-ready applications with minimal configuration.

## 📋 Database Terms

### **Composite Index**
A database index that includes multiple columns, optimizing queries that filter or sort by multiple fields.

### **Foreign Key**
A field in one table that refers to the primary key in another table, establishing relationships between tables.

### **MVCC (Multi-Version Concurrency Control)**
PostgreSQL's method for handling concurrent access to data by maintaining multiple versions of rows.

### **Normalized Database**
A database design that reduces redundancy by organizing data into separate related tables.

### **Primary Key**
A unique identifier for each row in a database table, ensuring no duplicate records.

### **Query Optimization**
The process of improving database query performance through indexing, query rewriting, and execution plan optimization.

### **VACUUM**
PostgreSQL maintenance operation that reclaims storage and updates statistics for query optimization.

## 🛰️ Satellite Data Terms

### **Clock Bias**
The difference between satellite clock time and true time, requiring correction for accurate positioning.

### **Common View**
A technique where multiple ground stations observe the same satellites simultaneously for time synchronization.

### **Ephemeris**
Orbital information that describes a satellite's position and velocity over time.

### **Pseudorange**
The apparent distance between a satellite and receiver, including clock and atmospheric errors.

### **Receiver**
Ground-based equipment that receives signals from navigation satellites.

### **Signal-in-Space (SIS)**
The navigation signal transmitted by satellites, containing timing and orbital information.

### **Time Difference**
The calculated difference between satellite clock times, used for synchronization analysis.

### **RINEX (Receiver Independent Exchange Format)**
A standard format for GNSS observation data exchange between different systems and organizations.

## 📊 Performance Terms

### **Benchmarking**
The process of measuring system performance against established standards or baseline metrics.

### **Caching**
Storing frequently accessed data in memory for faster retrieval.

### **Latency**
The time delay between a request and its response, typically measured in milliseconds.

### **Load Balancing**
Distributing incoming requests across multiple server instances to improve performance and reliability.

### **Scalability**
The ability of a system to handle increased load by adding resources (horizontal or vertical scaling).

### **Throughput**
The amount of data processed or requests handled per unit of time.

## 🔒 Security Terms

### **Authentication**
The process of verifying user or system identity before granting access.

### **Authorization**
The process of determining what actions an authenticated user or system is allowed to perform.

### **Input Validation**
The practice of checking user input for correctness, completeness, and security before processing.

### **SQL Injection**
A security vulnerability where malicious SQL code is inserted into application queries.

## 🔄 Development Terms

### **CI/CD (Continuous Integration/Continuous Deployment)**
Practices for automatically building, testing, and deploying code changes.

### **Code Review**
The systematic examination of source code by peers to identify bugs and improve quality.

### **Dependency Injection**
A design pattern where objects receive their dependencies from external sources rather than creating them internally.

### **Mock Objects**
Simulated objects used in testing to replace real dependencies and isolate units under test.

### **Refactoring**
The process of restructuring existing code without changing its external behavior to improve readability and maintainability.

### **Unit Testing**
Testing individual components or functions in isolation to verify they work correctly.

### **Version Control**
A system for tracking changes to code over time, typically using Git.

## 📋 System Administration Terms

### **Blue-Green Deployment**
A deployment strategy that uses two identical production environments to minimize downtime during updates.

### **Container**
A lightweight, portable package that includes an application and all its dependencies.

### **Disaster Recovery**
Plans and procedures for restoring system operations after a catastrophic failure.

### **Monitoring**
Continuous observation of system performance, health, and behavior to detect issues.

### **Service Level Agreement (SLA)**
A contract defining expected service performance and availability levels.

## 🔍 Data Quality Terms

### **Data Completeness**
The degree to which all required data elements are present in a dataset.

### **Data Integrity**
The assurance that data is accurate, consistent, and has not been corrupted.

### **Data Lineage**
The tracking of data from its origin through various transformations to its final destination.

### **Data Validation**
The process of ensuring data meets defined criteria for accuracy, completeness, and consistency.

### **Outlier Detection**
The identification of data points that deviate significantly from expected patterns.

## 📈 Analytics Terms

### **Aggregation**
The process of combining multiple data points into summary statistics like sums, averages, or counts.

### **Business Intelligence (BI)**
Technologies and strategies for analyzing business data to support decision-making.

### **Data Mining**
The process of discovering patterns and insights from large datasets.

### **Statistical Analysis**
The application of statistical methods to understand data patterns and relationships.

### **Time Series Analysis**
Analysis of data points collected over time to identify trends and patterns.

## 🔧 File Processing Terms

### **Batch Size**
The number of records processed together in a single operation for optimal performance.

### **Checksum**
A calculated value used to verify data integrity and detect corruption.

### **File Handle**
A reference to an open file that allows reading or writing operations.

### **Parsing**
The process of analyzing a string or file to extract structured information.

### **Stream Processing**
Processing data continuously as it flows through the system, rather than in batches.

---

**Acronyms Quick Reference:**

| Acronym | Full Form |
|---------|-----------|
| API | Application Programming Interface |
| CORS | Cross-Origin Resource Sharing |
| DTO | Data Transfer Object |
| IRNSS | Indian Regional Navigation Satellite System |
| JPA | Java Persistence API |
| MJD | Modified Julian Date |
| MVCC | Multi-Version Concurrency Control |
| REST | Representational State Transfer |
| RINEX | Receiver Independent Exchange Format |
| SIS | Signal-in-Space |
| SLA | Service Level Agreement |

**This glossary is maintained and updated as the system evolves. If you encounter terms not defined here, please suggest additions to keep the documentation comprehensive.**
