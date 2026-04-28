# Weather Viewer
 
A web application for tracking weather in your saved locations. Built with pure Spring MVC
 
## Features
 
- **Authentication** — registration and login with session-based auth via UUID cookies stored in the database
- **City search** — find a city by name via OpenWeather Geocoding API, get up to 5 matches with current weather
- **Saved locations** — add and remove locations from your personal list
- **Weather dashboard** — view current temperature, weather condition and icon for all saved locations
- **Session management** — configurable session TTL, scheduled cleanup of expired sessions
- **Password security** — BCrypt hashing with pepper and configurable rounds
## Tech Stack
 
| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring MVC 6 (no Spring Boot) |
| Templates | Thymeleaf 3.1 |
| ORM | Hibernate 6 + Jakarta Persistence |
| Connection pool | HikariCP |
| Migrations | Flyway |
| Database | PostgreSQL |
| Build | Gradle (WAR) |
| Server | Apache Tomcat (external) |
| External API | OpenWeather (Geocoding + Current Weather) |
 
## Project Structure
 
```
src/app/src/main/java/org/weather/app/
├── config/
│   ├── HibernateConfig.java       # DataSource, SessionFactory, Flyway, RestTemplate beans
│   └── WebConfig.java             # MVC config: interceptors, Thymeleaf, task scheduling
├── controller/
│   ├── IndexController.java       # GET /  → redirect
│   ├── LoginController.java       # GET/POST /login
│   ├── LogoutController.java      # POST /logout
│   ├── RegisterController.java    # GET/POST /register
│   └── WeatherController.java     # GET/POST /weather, /saveWeather, /deleteWeather
├── service/
│   ├── WeatherService.java        # OpenWeather API calls, DTO mapping
│   ├── SessionService.java        # Cookie parsing, session validation
│   ├── RegistrationService.java   # Login/password validation rules
│   └── PasswordService.java       # BCrypt + pepper hashing
├── repository/
│   ├── UserRepository.java
│   ├── LocationRepository.java
│   └── SessionRepository.java
├── model/                         # JPA entities: User, Location, UserSession
├── dto/                           # OpenWeatherGeoDto, OpenWeatherDataDto, SavedWeatherDto
├── SessionInterceptor.java        # HandlerInterceptor: validates session before protected routes
├── SessionCleanupTask.java        # @Scheduled: purges expired sessions
├── GlobalExceptionHandler.java    # @ControllerAdvice: 404 and generic error pages
└── WebAppInitializer.java         # WebApplicationInitializer: DispatcherServlet bootstrap
```
 
## Database Schema
 
Three tables, created and versioned by Flyway:
 
```sql
Users     (id, login, password)
Locations (id, name, user_id → Users, latitude, longitude)
Sessions  (id UUID, user_id → Users, expires_at)
```
 
## Configuration
 
Copy `example_secrets.properties` to `secrets.properties` and fill in the values:
 
```properties
# OpenWeather API
openweather.api.key=
openweather.data.url=https://api.openweathermap.org/data/2.5/weather
openweather.geo.url=http://api.openweathermap.org/geo/1.0/direct
 
# PostgreSQL
db.url=jdbc:postgresql://localhost:5432/weather
db.username=
db.password=
 
# Session TTL (values are summed)
session.duration.seconds=0
session.duration.minutes=0
session.duration.hours=24
 
# How often to run expired session cleanup (ms)
session.cleanup.rate.ms=60000
 
# Max concurrent sessions per user
session.amount=5
 
# Password hashing
password.pepper=
password.rounds=12
 
# Max city name length for search input
city.max.length=100
```
 
> `secrets.properties` is gitignored. Never commit it.
 
## Building & Running
 
**Prerequisites:** Java 21, Gradle, PostgreSQL, Apache Tomcat 10+
 
```bash
# Build WAR
cd src
./gradlew war
 
# The WAR ends up at:
# app/build/libs/app.war
 
# Deploy to Tomcat
cp app/build/libs/app.war $CATALINA_HOME/webapps/ROOT.war
$CATALINA_HOME/bin/startup.sh
```
 
Or use the custom Gradle task (requires a `deploy.sh` script at the project root):
 
```bash
./gradlew deployWar
```
 
## How It Works
 
**Request lifecycle:**
 
1. `WebAppInitializer` bootstraps `DispatcherServlet` with `WebConfig` + `HibernateConfig`
2. `SessionInterceptor` runs before every protected route — reads the UUID cookie, validates the session against the DB, and attaches `UserSession` as a request attribute
3. Controllers delegate to services, services talk to repositories via Hibernate `Session`
4. Thymeleaf renders the HTML response
5. `SessionCleanupTask` periodically deletes expired rows from the `Sessions` table
**Authentication flow:**
 
- On login a UUID session is saved to the DB with an expiry timestamp, then set as a cookie
- On every request `SessionInterceptor` checks the cookie UUID against the DB and verifies `expires_at > now()`
- On logout the session row is deleted and the cookie is cleared
