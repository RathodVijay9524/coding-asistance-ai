# Part 7 – Setup, Build & Run

## 7.1 Prerequisites

- **Java:** 17+ (or as specified in `pom.xml`)
- **Maven:** 3.6+
- **MySQL:** 8.x
- **Port:** 8080 (default Spring Boot port)

## 7.2 Database Setup (MySQL)

1. Create the database:

```sql
CREATE DATABASE IF NOT EXISTS `coding-assistance`
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

2. Update credentials in `src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/coding-assistance?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=YOUR_USER
spring.datasource.password=YOUR_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate

spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

spring.datasource.hikari.maximum-pool-size=10
```

3. On first run, Flyway will execute `V1__Create_Edit_History_Tables.sql` and create all necessary tables, views, and indexes.

## 7.3 Build

From project root (`Coding-Assistance`):

```bash
mvn clean package
```

If you want to skip tests during build:

```bash
mvn clean package -DskipTests
```

## 7.4 Run

Using Maven:

```bash
mvn spring-boot:run
```

Or using the packaged JAR:

```bash
java -jar target/Coding-Assistance-0.0.1-SNAPSHOT.jar
```

Once started, the application should log something like:

```text
:: Spring Boot ::  (v3.x)
Started Coding-Assistance in X.XXX seconds
```

## 7.5 Smoke Tests (cURL)

### 7.5.1 Track an Edit

```bash
curl -X POST http://localhost:8080/api/edits/track \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "filePath": "src/main/java/Test.java",
    "originalCode": "public void test() {}",
    "editedCode": "public void testMethod() {}",
    "editType": "rename_method",
    "suggestionSource": "AI",
    "accepted": true,
    "description": "Renamed method"
  }'
```

### 7.5.2 Record Feedback

```bash
curl -X POST http://localhost:8080/api/feedback/record \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "suggestionId": 1,
    "rating": 5,
    "action": "accepted",
    "helpful": true,
    "sentiment": "positive"
  }'
```

### 7.5.3 Generate Inline Suggestions

```bash
curl -X POST http://localhost:8080/api/suggestions/inline \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "code": "public void test() { int a = 1; int b = 2; }",
    "language": "java",
    "cursorPosition": 45,
    "context": "method"
  }'
```

### 7.5.4 Generate Unit Tests

```bash
curl -X POST http://localhost:8080/api/tests/generate-unit \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "language": "java",
    "framework": "junit5",
    "sourceCode": "public class Calculator { public int add(int a, int b) { return a + b; } }"
  }'
```

## 7.6 Troubleshooting

- If you see **bean creation errors** after refactors, run:

```bash
mvn clean package
```

- If Flyway fails, verify:
  - Database exists
  - Credentials are correct
  - No manual schema changes conflict with migration

- Use logs at DEBUG level for SQL queries by adjusting:

```properties
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
```
