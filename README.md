# 🏢 Meeting Room Booking System

A full-stack web application that streamlines meeting room bookings with real-time availability tracking and conflict prevention. Built with React.js and Spring Boot, this system provides an intuitive interface for scheduling and managing meeting rooms throughout the day.

![React](https://img.shields.io/badge/React-18.x-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-green)
![Redis](https://img.shields.io/badge/Redis-7.x-red)
![Keycloak](https://img.shields.io/badge/Keycloak-23.x-orange)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple)

## 📋 Table of Contents

- [About The Project](#about-the-project)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)
- [Security](#security)
- [Contributing](#contributing)
- [License](#license)
- [Contact](#contact)

## 🎯 About The Project

This project was created to solve a common workplace challenge - efficiently managing meeting room bookings and preventing scheduling conflicts. The application provides:

- **Visual Timeline Interface**: Easy-to-view daily schedule from 9 AM to 6 PM
- **Real-time Availability**: Instant visibility of booked and available time slots
- **Conflict Prevention**: Automatic validation to prevent double-booking
- **Quick Booking**: Simple one-click booking with minimal information required
- **Secure Authentication**: Enterprise-grade security with Keycloak integration
- **High Performance**: Redis caching for lightning-fast response times

## ✨ Features

### Core Features (MVP)
- ✅ Timeline view for current day (9 AM - 6 PM)
- ✅ Visual display of 3-4 meeting rooms
- ✅ Clear indication of booked vs available slots
- ✅ Quick booking with name input
- ✅ Double-booking prevention system
- ✅ Real-time booking validation

### Bonus Features
- ✅ Variable duration booking (30, 45, 60 minutes)
- ✅ Early release functionality for concluded meetings
- ✅ Toast notifications for user feedback
- ✅ Responsive design for mobile and desktop
- ✅ Secure user authentication
- ✅ Redis caching for improved performance
- ✅ State management with Redux

## 🛠️ Tech Stack

### Frontend
- **React.js** (v18.x) - UI Framework
- **Redux** - State Management
- **Bootstrap** (v5.3) - Styling & Responsiveness
- **React Toastify** - Notifications
- **Axios** - HTTP Client
- **React Router DOM** - Navigation

### Backend
- **Spring Boot** (v3.x) - Application Framework
- **Java** - Programming Language
- **Spring Security** - Security Framework
- **Spring Data JPA** - Data Persistence
- **Redis** - Caching Layer
- **Keycloak** - Authentication & Authorization

### Infrastructure
- **Keycloak** (Port 8180) - Identity & Access Management
- **Redis** (Port 6379) - In-Memory Cache
- **Spring Boot Server** (Port 8080) - Backend API
- **React Dev Server** (Port 3000) - Frontend Application

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Node.js**: v16.x or higher
- **npm**: v8.x or higher
- **Java**: JDK 17 or higher
- **Maven**: v3.8 or higher
- **Docker**: (Optional) For Redis and Keycloak containers
- **Redis**: v7.x
- **Keycloak**: v23.x

Check your versions:
```bash
node -v
npm -v
java -version
mvn -v
docker -v
```

## 🚀 Installation

### 1. Clone the Repository
```bash
git clone https://github.com/yourusername/meeting-room-booking-system.git
cd meeting-room-booking-system
```

### 2. Setup Redis

#### Using Docker:
```bash
docker run -d -p 6379:6379 --name redis-cache redis:latest
```

#### Manual Installation:
Download and install Redis from [redis.io](https://redis.io/download)
```bash
redis-server --port 6379
```

### 3. Setup Keycloak

#### Using Docker:
```bash
docker run -d -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  --name keycloak \
  quay.io/keycloak/keycloak:latest start-dev
```

#### Configure Keycloak:
1. Access Keycloak admin console: http://localhost:8180
2. Create a new realm: `meeting-room-realm`
3. Create a client: `meeting-room-client`
4. Configure redirect URIs: `http://localhost:3000/*`
5. Create roles: `user`, `admin`
6. Create test users

### 4. Install Backend Dependencies
```bash
cd backend
mvn clean install
```

### 5. Install Frontend Dependencies
```bash
cd ../frontend
npm install
```

### 6. Environment Setup

#### Backend (application.properties)
Create `src/main/resources/application.properties`:
```properties
# Server Configuration
server.port=8080

# Redis Configuration
spring.redis.host=localhost
spring.redis.port=6379
spring.cache.type=redis

# Keycloak Configuration
keycloak.auth-server-url=http://localhost:8180
keycloak.realm=meeting-room-realm
keycloak.resource=meeting-room-client
keycloak.credentials.secret=your-client-secret

# Database Configuration (Optional - if using database)
spring.datasource.url=jdbc:h2:mem:meetingdb
spring.jpa.hibernate.ddl-auto=update
```

#### Frontend (.env)
Create a `.env` file in the `frontend` folder:
```env
REACT_APP_API_URL=http://localhost:8080/api
REACT_APP_KEYCLOAK_URL=http://localhost:8180
REACT_APP_KEYCLOAK_REALM=meeting-room-realm
REACT_APP_KEYCLOAK_CLIENT_ID=meeting-room-client
```

## 🎮 Usage

### Running the Application

#### 1. Start Redis
```bash
redis-server --port 6379
```

#### 2. Start Keycloak
```bash
# If using Docker
docker start keycloak

# Or access at http://localhost:8180
```

#### 3. Start the Backend Server
```bash
cd backend
mvn spring-boot:run
```
The backend will run on **http://localhost:8080**

#### 4. Start the Frontend Application
Open a new terminal:
```bash
cd frontend
npm start
```
The frontend will run on **http://localhost:3000**

### Using the Application

1. **Login**: Authenticate using Keycloak credentials
2. **View Schedule**: See the daily timeline (9 AM - 6 PM) for all meeting rooms
3. **Check Availability**: Green slots indicate available time slots
4. **Book a Room**: 
   - Click on an available time slot
   - Enter your name
   - Select duration (30, 45, or 60 minutes)
   - Click "Book Now"
5. **View Confirmation**: Receive toast notification confirming your booking
6. **Early Release**: Click on your booking to release the room early if meeting concludes ahead of schedule

## 🔌 API Endpoints

### Base URL: `http://localhost:8080/api`

#### 1. Get All Meeting Rooms
```http
GET /rooms
Authorization: Bearer {token}
```
**Response:**
```json
{
  "success": true,
  "rooms": [
    {
      "id": 1,
      "name": "Lagos Meeting Blue",
      "capacity": 10,
      "floor": 2
    },
    {
      "id": 2,
      "name": "Mumbai Conference Red",
      "capacity": 8,
      "floor": 3
    }
  ]
}
```

#### 2. Get Daily Schedule
```http
GET /bookings/schedule?date=2025-11-22
Authorization: Bearer {token}
```
**Response:**
```json
{
  "success": true,
  "date": "2025-11-22",
  "schedule": [
    {
      "roomId": 1,
      "roomName": "Lagos Meeting Blue",
      "bookings": [
        {
          "id": 101,
          "startTime": "14:00",
          "endTime": "15:00",
          "bookedBy": "John Doe",
          "status": "CONFIRMED"
        }
      ]
    }
  ]
}
```

#### 3. Create Booking
```http
POST /bookings
Authorization: Bearer {token}
Content-Type: application/json

{
  "roomId": 1,
  "date": "2025-11-22",
  "startTime": "16:00",
  "duration": 60,
  "bookedBy": "Jane Smith"
}
```
**Response:**
```json
{
  "success": true,
  "booking": {
    "id": 102,
    "roomId": 1,
    "roomName": "Lagos Meeting Blue",
    "date": "2025-11-22",
    "startTime": "16:00",
    "endTime": "17:00",
    "bookedBy": "Jane Smith",
    "status": "CONFIRMED"
  }
}
```

#### 4. Check Availability
```http
GET /bookings/availability?roomId=1&date=2025-11-22&startTime=14:30
Authorization: Bearer {token}
```
**Response:**
```json
{
  "success": false,
  "available": false,
  "message": "Room is already booked from 2:00 PM to 3:00 PM",
  "conflictingBooking": {
    "id": 101,
    "startTime": "14:00",
    "endTime": "15:00"
  }
}
```

#### 5. Release Booking Early
```http
PUT /bookings/{bookingId}/release
Authorization: Bearer {token}
```
**Response:**
```json
{
  "success": true,
  "message": "Booking released successfully",
  "newEndTime": "14:30"
}
```

#### 6. Cancel Booking
```http
DELETE /bookings/{bookingId}
Authorization: Bearer {token}
```
**Response:**
```json
{
  "success": true,
  "message": "Booking cancelled successfully"
}
```

## 📁 Project Structure
```
meeting-room-booking-system/
├── backend/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   │   └── com/meetingroom/
│   │   │   │       ├── config/
│   │   │   │       │   ├── SecurityConfig.java
│   │   │   │       │   ├── RedisConfig.java
│   │   │   │       │   └── KeycloakConfig.java
│   │   │   │       ├── controller/
│   │   │   │       │   ├── RoomController.java
│   │   │   │       │   └── BookingController.java
│   │   │   │       ├── model/
│   │   │   │       │   ├── Room.java
│   │   │   │       │   ├── Booking.java
│   │   │   │       │   └── User.java
│   │   │   │       ├── repository/
│   │   │   │       │   ├── RoomRepository.java
│   │   │   │       │   └── BookingRepository.java
│   │   │   │       ├── service/
│   │   │   │       │   ├── RoomService.java
│   │   │   │       │   ├── BookingService.java
│   │   │   │       │   └── CacheService.java
│   │   │   │       ├── dto/
│   │   │   │       │   ├── BookingRequest.java
│   │   │   │       │   └── BookingResponse.java
│   │   │   │       ├── exception/
│   │   │   │       │   ├── BookingConflictException.java
│   │   │   │       │   └── GlobalExceptionHandler.java
│   │   │   │       └── MeetingRoomApplication.java
│   │   │   └── resources/
│   │   │       ├── application.properties
│   │   │       └── data.sql
│   │   └── test/
│   ├── pom.xml
│   └── .gitignore
│
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── components/
│   │   │   ├── Timeline.jsx
│   │   │   ├── RoomCard.jsx
│   │   │   ├── BookingModal.jsx
│   │   │   ├── TimeSlot.jsx
│   │   │   └── Navbar.jsx
│   │   ├── pages/
│   │   │   ├── Dashboard.jsx
│   │   │   ├── MyBookings.jsx
│   │   │   └── Login.jsx
│   │   ├── redux/
│   │   │   ├── store.js
│   │   │   ├── slices/
│   │   │   │   ├── bookingSlice.js
│   │   │   │   ├── roomSlice.js
│   │   │   │   └── authSlice.js
│   │   ├── services/
│   │   │   ├── api.js
│   │   │   ├── keycloak.js
│   │   │   └── bookingService.js
│   │   ├── utils/
│   │   │   ├── timeUtils.js
│   │   │   └── validators.js
│   │   ├── styles/
│   │   │   └── custom.css
│   │   ├── App.js
│   │   └── index.js
│   ├── .env
│   ├── .gitignore
│   └── package.json
│
├── docker-compose.yml
├── .gitignore
└── README.md
```

## 🔒 Security

### Authentication & Authorization
The application uses **Keycloak** for enterprise-grade security:

- **OAuth 2.0 / OpenID Connect**: Industry-standard authentication protocols
- **JWT Tokens**: Secure token-based authentication
- **Role-Based Access Control (RBAC)**: Different permissions for users and admins
- **Single Sign-On (SSO)**: Seamless authentication across services

### Spring Security Configuration
- All API endpoints are protected by default
- JWT token validation on every request
- CORS configuration for secure cross-origin requests
- CSRF protection enabled

### Redis Security
- Password-protected Redis instance (recommended for production)
- Connection pooling for efficient resource management
- TTL (Time To Live) policies for cached data

## 🎨 Booking Time Slots

### Available Time Slots
The system operates from **9:00 AM to 6:00 PM** with the following slot structure:

| Time Range | Slot Duration |
|-----------|--------------|
| 9:00 AM - 6:00 PM | 30 minutes (minimum) |
| Customizable | 30, 45, or 60 minutes |

### Duration Options
- **30 minutes**: Quick meetings, stand-ups
- **45 minutes**: Standard team meetings
- **60 minutes**: Detailed discussions, interviews

## 🏗️ Meeting Rooms

The system manages 3-4 meeting rooms with the following structure:

| Room Name | Capacity | Floor | Amenities |
|-----------|----------|-------|-----------|
| Lagos Meeting Blue | 10 | 2 | Projector, Whiteboard |
| Mumbai Conference Red | 8 | 3 | Video Conference, TV |
| Tokyo Discussion Green | 6 | 2 | Whiteboard |
| Berlin Innovation Yellow | 12 | 4 | Smart Board, Video Conference |

## 🚦 Conflict Prevention System

The application implements a robust double-booking prevention system:

### Validation Rules
1. **Time Overlap Check**: Validates if requested time conflicts with existing bookings
2. **Room Availability**: Ensures room is not already booked for the time slot
3. **Duration Validation**: Verifies booking doesn't extend beyond operating hours (6 PM)
4. **Minimum Gap**: Ensures proper cleanup time between bookings

### Example Scenarios

**✅ Success Case:**
```
Room: Lagos Meeting Blue
Existing Booking: 2:00 PM - 3:00 PM
New Request: 4:00 PM - 5:00 PM
Result: ✅ Booking Confirmed
```

**❌ Conflict Case:**
```
Room: Lagos Meeting Blue
Existing Booking: 2:00 PM - 3:00 PM
New Request: 2:30 PM - 3:30 PM
Result: ❌ Error - "Room is already booked from 2:00 PM to 3:00 PM"
```

## 🚀 Performance Optimization

### Redis Caching Strategy
The application uses Redis to cache frequently accessed data:

- **Room Information**: Cached for 1 hour
- **Daily Schedule**: Cached for 15 minutes
- **Availability Checks**: Real-time with cache invalidation
- **User Sessions**: Stored in Redis for fast authentication

### Cache Invalidation
- Automatic cache invalidation on booking creation
- Manual cache refresh option for admins
- TTL-based expiration for all cached data

## 📱 Responsive Design

The application is fully responsive and works seamlessly across:
- **Desktop**: Full-featured timeline view
- **Tablet**: Optimized grid layout
- **Mobile**: Streamlined booking interface with touch-friendly controls

---

**Project Link**: [https://github.com/yourusername/meeting-room-booking-system](https://github.com/yourusername/meeting-room-bookin
