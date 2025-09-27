# Dynamic API Mock Server - Enhancements

This document outlines the comprehensive enhancements added to the Dynamic API Mock Server, transforming it into a production-ready, feature-rich mock server solution.

## 🚀 Enhanced Features

### 1. Per-Mock Request Matching on Headers/Query/Body

**What it does:**
- Advanced request matching based on headers, query parameters, and request body patterns
- Priority-based matching system for complex scenarios
- Support for regex patterns, exact matches, and conditional matching

**Key Components:**
- `RequestMatchingService` - Handles complex matching logic
- Enhanced `MockEndpoint` entity with matching criteria fields
- Pattern matching support (regex, contains, startsWith, endsWith)

**Usage Examples:**
```json
{
  "endpoint": "/api/users",
  "method": "GET",
  "requestHeaders": "{\"Authorization\": \"Bearer.*\"}",
  "queryParameters": "{\"page\": \"\\\\d+\"}",
  "requestBodyPattern": "{\"userId\": \"\\\\d+\"}",
  "priority": 10
}
```

### 2. Dynamic Templating (Handlebars) for Response JSON

**What it does:**
- Dynamic response generation using Handlebars templating
- Access to request context (headers, query params, body)
- Built-in helpers for common use cases

**Key Components:**
- `TemplatingService` - Handlebars template processing
- Custom helpers for random data, timestamps, UUIDs
- Request context injection

**Template Examples:**
```json
{
  "id": "{{random 1 1000}}",
  "name": "{{request.body.name}}",
  "createdAt": "{{date}}",
  "uuid": "{{uuid}}",
  "timestamp": "{{timestamp}}"
}
```

**Available Helpers:**
- `{{random min max}}` - Random number generation
- `{{randomString length}}` - Random string generation
- `{{uuid}}` - UUID generation
- `{{timestamp}}` - Current timestamp
- `{{date format}}` - Formatted date
- `{{array size itemTemplate}}` - Array generation

### 3. Import/Export Functionality for Mocks (JSON)

**What it does:**
- Export all mocks or selected mocks to JSON
- Import mocks from JSON files
- Template generation for easy setup
- Bulk operations support

**Key Components:**
- `ImportExportService` - Handles import/export logic
- `ImportExportController` - REST endpoints for operations
- Support for file uploads and direct JSON input

**API Endpoints:**
- `GET /api/mocks/export` - Export all mocks
- `POST /api/mocks/export/selected` - Export selected mocks
- `POST /api/mocks/import` - Import from JSON
- `POST /api/mocks/import/file` - Import from file
- `GET /api/mocks/export/template` - Download import template

### 4. Role-Based Access Control and Rate Limiting

**What it does:**
- User roles: USER, ADMIN, SUPER_ADMIN
- Per-user rate limiting configuration
- IP-based rate limiting fallback
- Admin management interface

**Key Components:**
- Enhanced `User` entity with roles and rate limits
- `RateLimitingService` - Bucket4j-based rate limiting
- `RoleBasedAccessService` - Role-based permissions
- `AdminController` - User and rate limit management

**Rate Limiting Features:**
- User-specific rate limits (requests per minute)
- IP-based fallback rate limiting
- Admin controls for rate limit management
- Real-time rate limit status monitoring

### 5. Paging/Filtering for Mock Listing and UI Console

**What it does:**
- Paginated mock endpoint listing
- Advanced filtering by method, status, endpoint
- Full-text search across endpoints and responses
- Modern web-based UI console

**Key Components:**
- `PaginationService` - Generic pagination utilities
- Enhanced repository with custom queries
- `UIController` - Serves the web console
- Responsive HTML5 interface

**UI Features:**
- Real-time search and filtering
- Inline editing of mock endpoints
- Bulk operations support
- Responsive design
- Modern JavaScript interface

### 6. Spring Boot Actuator Endpoints for Health/Metrics

**What it does:**
- Comprehensive health monitoring
- Custom metrics collection
- Prometheus metrics export
- Application insights

**Key Components:**
- `MockServerHealthIndicator` - Custom health checks
- `MockServerMetrics` - Micrometer-based metrics
- Prometheus integration
- Custom application metrics

**Available Endpoints:**
- `/actuator/health` - Application health status
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/info` - Application information

**Custom Metrics:**
- `mock_requests_total` - Total mock requests
- `mock_requests_by_method` - Requests by HTTP method
- `mock_requests_by_status` - Requests by response status
- `mock_endpoints_total` - Total mock endpoints
- `mock_endpoints_active` - Active mock endpoints
- `users_total` - Total users

### 7. Seed Demo Data Profiles

**What it does:**
- Pre-configured demo users and mock endpoints
- Realistic test scenarios
- Template examples
- Easy setup for development/testing

**Key Components:**
- `DataSeeder` - CommandLineRunner for data initialization
- `DemoDataController` - Management endpoints
- Pre-configured user roles and permissions
- Comprehensive mock endpoint examples

**Demo Users:**
- `admin` / `admin123` - Admin user with high rate limits
- `developer` / `dev123` - Developer user with moderate limits
- `tester` / `test123` - Tester user with basic limits

**Demo Mock Endpoints:**
- User management APIs
- Product catalog APIs
- Authentication endpoints
- Error scenario testing
- Rate limiting examples
- Complex templated responses

## 🛠️ Technical Implementation

### Database Schema Updates
- Enhanced `MockEndpoint` entity with matching criteria
- User role and rate limiting fields
- Proper indexing for performance

### Security Enhancements
- JWT-based authentication
- Role-based authorization
- Rate limiting with Bucket4j
- CORS configuration

### Performance Optimizations
- Pagination for large datasets
- Efficient database queries
- Caching for rate limiting
- Async processing where appropriate

### Monitoring and Observability
- Comprehensive health checks
- Custom metrics collection
- Prometheus integration
- Request/response logging

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 12+
- Maven 3.6+

### Installation
1. Clone the repository
2. Configure database connection in `application.yml`
3. Run `mvn spring-boot:run`
4. Access the console at `http://localhost:8088/console`

### Demo Data Setup
1. Access `/api/demo/seed` to populate demo data
2. Use demo credentials to test different user roles
3. Explore the UI console for mock management

### API Documentation
- Swagger UI: `http://localhost:8088/swagger-ui.html`
- OpenAPI spec: `http://localhost:8088/v3/api-docs`

## 📊 Monitoring

### Health Checks
- Application health: `/actuator/health`
- Database connectivity
- Mock endpoint statistics
- User activity metrics

### Metrics
- Prometheus metrics: `/actuator/prometheus`
- Custom application metrics
- Request/response statistics
- Rate limiting metrics

## 🔧 Configuration

### Rate Limiting
```yaml
# Per-user rate limits can be configured via admin interface
# Default limits:
# - Admin: 1000 requests/minute
# - Developer: 200 requests/minute
# - Tester: 100 requests/minute
```

### Templating
```yaml
# Handlebars templates are processed automatically
# when response JSON contains {{ }} syntax
```

### Security
```yaml
# JWT configuration
app:
  jwt:
    secret: "your-secret-key"
    expiration-ms: 86400000
```

## 🎯 Use Cases

### Development
- API mocking for frontend development
- Backend service simulation
- Integration testing scenarios

### Testing
- Automated test data generation
- Error scenario simulation
- Performance testing with rate limits

### Production
- API gateway simulation
- Service mesh testing
- Load testing with realistic responses

## 🔮 Future Enhancements

- GraphQL endpoint support
- WebSocket mock endpoints
- Advanced request/response validation
- Mock endpoint versioning
- Team collaboration features
- Advanced analytics dashboard

## 📝 API Examples

### Creating a Mock with Advanced Matching
```bash
curl -X POST http://localhost:8088/mock/endpoints \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer your-jwt-token" \
  -d '{
    "endpoint": "/api/users",
    "method": "GET",
    "responseJson": "{\"users\": [{\"id\": {{random 1 100}}, \"name\": \"{{randomString 10}}\"}]}",
    "requestHeaders": "{\"Authorization\": \"Bearer.*\"}",
    "queryParameters": "{\"page\": \"\\\\d+\"}",
    "priority": 10,
    "isActive": true
  }'
```

### Testing Rate Limiting
```bash
# Make multiple requests to test rate limiting
for i in {1..10}; do
  curl -X GET http://localhost:8088/mock/api/rate-limit
done
```

### Exporting Mocks
```bash
curl -X GET http://localhost:8088/api/mocks/export \
  -H "Authorization: Bearer your-jwt-token" \
  -o mocks_export.json
```

This enhanced Dynamic API Mock Server now provides a comprehensive solution for API mocking, testing, and development with enterprise-grade features and monitoring capabilities.
