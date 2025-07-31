# URL Shortener Service

A simple and efficient URL shortening service built with Spring Boot and MongoDB. This project allows you to create short URLs, track access statistics, and manage your shortened links through a RESTful API.

> **Assignment Project**: This project is part of the [URL Shortening Service](https://roadmap.sh/projects/url-shortening-service) assignment from roadmap.sh.

## Features

- ✅ **Shorten URLs**: Convert long URLs into short, manageable links
- ✅ **Redirect**: Automatically redirect short URLs to original destinations
- ✅ **Statistics**: Track access counts and timestamps for each shortened URL
- ✅ **Update URLs**: Modify the destination of existing short codes
- ✅ **Delete URLs**: Remove shortened URLs when no longer needed
- ✅ **URL Validation**: Ensure only valid HTTP/HTTPS URLs are accepted
- ✅ **Duplicate Prevention**: Reuse existing short codes for duplicate URLs

## Live Demo

🚀 **The application is live and deployed on Azure!**

**Production URL:** https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/

Try it out directly:
```bash
# Create a short URL
curl -X POST https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten \
  -H "Content-Type: application/json" \
  -d "https://www.example.com/your-long-url"
```

## Technologies Used

- **Java 21** - Programming language
- **Spring Boot 3.5.4** - Application framework
- **Spring Data MongoDB** - Database integration
- **MongoDB** - NoSQL database for data storage
- **Maven** - Build tool and dependency management
- **SLF4J** - Logging framework

## Prerequisites

Before running this application, make sure you have:

- **Java 21** or higher installed
- **Maven 3.6+** installed
- **MongoDB** running locally on port 27017 (or configure a remote instance)

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd url-shortener
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Configure MongoDB** (Optional)
   
   By default, the application connects to `mongodb://localhost:27017` with database name `url_shortener`. To use different settings, you can:
   
   - Set environment variables:
     ```bash
     export MONGO_URI=mongodb://your-mongodb-uri
     export MONGO_DATABASE=your-database-name
     ```
   
   - Or modify `src/main/resources/application.properties`

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

   The application will start on `http://localhost:8080`

## Deployment

This application is currently deployed and running on **Azure Container Apps** at:
- **Production URL**: https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/
- **API Base URL**: https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api

The deployment uses:
- **Azure Container Apps** for hosting
- **MongoDB Atlas** for the database (free tier)
- **Docker containerization** for consistent deployment

You can use the live service immediately without any setup!

## API Endpoints

### Production URL: `https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api`
### Local Development URL: `http://localhost:8080/api`

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/shorten` | Create a shortened URL |
| GET | `/{shortCode}` | Redirect to original URL |
| PUT | `/shorten/{shortCode}` | Update an existing URL |
| DELETE | `/shorten/{shortCode}` | Delete a shortened URL |
| GET | `/shorten/{shortCode}/stats` | Get URL statistics |
| GET | `/health` | Health check endpoint |

### Example Usage

#### 1. Shorten a URL
```bash
# Request (Production)
curl -X POST https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten \
  -H "Content-Type: application/json" \
  -d "https://www.example.com/very/long/url/that/needs/shortening"

# Response
{
  "id": "507f1f77bcf86cd799439011",
  "url": "https://www.example.com/very/long/url/that/needs/shortening",
  "shortCode": "https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/abc123",
  "createdAt": "2025-01-29T10:30:00Z",
  "updatedAt": "2025-01-29T10:30:00Z"
}
```

#### 2. Access a shortened URL
```bash
# Visit in browser or curl (Production)
curl -L https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/abc123
# Redirects to: https://www.example.com/very/long/url/that/needs/shortening
```

#### 3. Get URL statistics
```bash
# Request (Production)
curl https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123/stats

# Response
{
  "id": "507f1f77bcf86cd799439011",
  "originalUrl": "https://www.example.com/very/long/url/that/needs/shortening",
  "shortCode": "abc123",
  "createdAt": "2025-01-29T10:30:00Z",
  "updatedAt": "2025-01-29T10:30:00Z",
  "accessCount": 5
}
```

#### 4. Update a URL
```bash
# Request (Production)
curl -X PUT https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123 \
  -H "Content-Type: application/json" \
  -d "https://www.updated-example.com"
```

#### 5. Delete a URL
```bash
# Request (Production)
curl -X DELETE https://url-shortener.proudmoss-71bb2d25.southeastasia.azurecontainerapps.io/api/shorten/abc123
```

## Project Structure

```
url-shortener/
├── src/
│   ├── main/
│   │   ├── java/com/mjfactor/url_shortener/
│   │   │   ├── UrlShortenerApplication.java    # Main application & REST controller
│   │   │   ├── UrlEntity.java                  # MongoDB entity model
│   │   │   └── UrlRepository.java              # Database repository interface
│   │   └── resources/
│   │       └── application.properties          # Application configuration
│   └── test/
│       └── java/com/mjfactor/url_shortener/
│           └── UrlShortenerApplicationTests.java
├── pom.xml                                     # Maven dependencies
└── README.md                                   # This file
```

## Configuration

The application can be configured using environment variables or by modifying `application.properties`:

```properties
# Database Configuration
spring.data.mongodb.uri=${MONGO_URI:mongodb://localhost:27017}
spring.data.mongodb.database=${MONGO_DATABASE:url_shortener}

# Server Configuration
server.port=${SERVER_PORT:8080}

# Logging Configuration
logging.level.com.mjfactor.url_shortener=${LOG_LEVEL:INFO}
```

## Data Model

Each shortened URL is stored with the following fields:

- **id**: MongoDB ObjectId (auto-generated)
- **originalUrl**: The original long URL
- **shortCode**: Unique 6-character identifier
- **createdAt**: Timestamp when URL was first created
- **updatedAt**: Timestamp when URL was last modified
- **accessCount**: Number of times the short URL has been accessed

## Error Handling

The API returns appropriate HTTP status codes and error messages:

- **400 Bad Request**: Invalid URL format or validation errors
- **404 Not Found**: Short code doesn't exist
- **201 Created**: Successfully created new shortened URL
- **200 OK**: Successful retrieval or update
- **204 No Content**: Successful deletion
- **302 Found**: Successful redirect

## Development

To run in development mode with auto-reload:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

To run tests:

```bash
mvn test
```

## License

This project is created for educational purposes as part of the roadmap.sh URL Shortening Service assignment.
