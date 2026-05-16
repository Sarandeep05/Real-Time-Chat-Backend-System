# 💬 Real-Time Chat Backend System

A production-ready, real-time chat backend built with **Spring Boot 3**, **MongoDB**, and **WebSockets (STOMP)**. Supports one-to-one and group messaging, JWT authentication, read receipts, and online presence tracking.

## 🏗️ Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Framework | Spring Boot 3.4.5 |
| Database | MongoDB (Atlas or local) |
| Real-time | WebSocket + STOMP |
| Auth | JWT (jjwt 0.12.5) |
| Build Tool | Gradle |
| API Docs | SpringDoc OpenAPI (Swagger UI) |

---

## 🌐 Live Deployment

| Resource | URL |
|---|---|
| **API Base URL** | `https://<your-app-name>.onrender.com` |
| **Swagger API Docs** | `https://<your-app-name>.onrender.com/swagger-ui/index.html` |
| **GitHub Repository** | `https://github.com/Sarandeep05/Real-Time-Chat-Backend-System` |

---

## 🚀 Getting Started

### Prerequisites
- Java 17+
- MongoDB instance (local or [MongoDB Atlas](https://cloud.mongodb.com))

### Clone & Run

```bash
git clone https://github.com/Sarandeep05/Real-Time-Chat-Backend-System.git
cd Real-Time-Chat-Backend-System

# Set environment variables (or edit application.properties)
export MONGO_URI="mongodb://localhost:27017/chatdb"
export JWT_SECRET="your-super-secret-key-at-least-32-chars-long"
export JWT_EXPIRATION=86400000  # 24 hours in ms

# Run
./gradlew bootRun
```

The API will be available at `http://localhost:8080`.

---

## 🔑 Environment Variables

| Variable | Default | Description |
|---|---|---|
| `MONGO_URI` | `mongodb://localhost:27017/chatdb` | MongoDB connection string |
| `JWT_SECRET` | *(insecure default)* | HMAC-SHA256 signing key (min 32 chars) |
| `JWT_EXPIRATION` | `86400000` | Token expiry in milliseconds (24h) |
| `PORT` | `8080` | Server port |

> **⚠️ Security:** Always override `JWT_SECRET` in production. Never commit secrets to source control.

---

## 📖 API Documentation

Interactive API documentation is available via **Swagger UI**:
- **Local:** `http://localhost:8080/swagger-ui/index.html`
- **Production:** `https://<your-app-name>.onrender.com/swagger-ui/index.html`

Swagger UI includes a built-in **Authorize** button to test secured endpoints with JWT tokens.

---

## 📡 REST API Reference

### Auth (Public)

#### Register
```
POST /auth/register
Content-Type: application/json

{
  "name": "Alice",
  "email": "alice@example.com",
  "password": "password123"
}
```

**Response 201:**
```json
{
  "token": "<JWT>",
  "userId": "...",
  "name": "Alice",
  "email": "alice@example.com"
}
```

#### Login
```
POST /auth/login
Content-Type: application/json

{
  "email": "alice@example.com",
  "password": "password123"
}
```

---

### Users (🔒 JWT Required)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/users` | List all users |
| GET | `/users/{id}` | Get user by ID |

**Authorization header:** `Bearer <token>`

---

### Chat History (🔒 JWT Required)

| Method | Endpoint | Description |
|---|---|---|
| GET | `/chats/direct?user1={id}&user2={id}&page=0&size=20` | Paginated 1-to-1 history |
| GET | `/chats/group/{groupId}?page=0&size=20` | Paginated group history |

---

### Groups (🔒 JWT Required)

| Method | Endpoint | Description |
|---|---|---|
| POST | `/groups` | Create group |
| GET | `/groups` | List my groups |
| GET | `/groups/{groupId}` | Get group details |
| POST | `/groups/{groupId}/members/{userId}` | Add member |
| DELETE | `/groups/{groupId}/members/{userId}` | Remove member |

**Create Group Body:**
```json
{
  "name": "Team Alpha",
  "memberIds": ["userId1", "userId2"]
}
```

---

## 🔌 WebSocket API

### Connection

Connect to `ws://localhost:8080/ws` (or `/ws` with SockJS).

Send JWT in the STOMP CONNECT frame headers:
```javascript
const headers = { Authorization: 'Bearer <your-jwt-token>' };
stompClient.connect(headers, onConnected);
```

### Destinations Summary

| Direction | Destination | Purpose |
|---|---|---|
| Client → Server | `/app/chat.send` | Send a direct message |
| Client → Server | `/app/chat.group.send` | Send a group message |
| Client → Server | `/app/chat.receipt` | Send a read receipt |
| Server → Client | `/user/queue/messages` | Receive direct messages |
| Server → Client | `/topic/group/{groupId}` | Receive group messages |
| Server → Client | `/user/queue/receipts` | Receive read receipt confirmations |
| Server → Client | `/user/queue/errors` | Receive error notifications |
| Server → Client | `/topic/presence` | User online/offline events |

### Send Direct Message
```json
// Send to: /app/chat.send
{
  "senderId": "alice-id",
  "receiverId": "bob-id",
  "type": "DIRECT",
  "content": "Hello Bob!"
}
```

### Send Group Message
```json
// Send to: /app/chat.group.send
{
  "senderId": "alice-id",
  "receiverId": "group-id",
  "type": "GROUP",
  "content": "Hello everyone!"
}
```

### Mark Message as Read
```json
// Send to: /app/chat.receipt
{
  "messageId": "msg-id",
  "readerId": "bob-id"
}
```

---

## 🏛️ Architecture

```
├── controller/
│   ├── AuthController.java          # POST /auth/register, /auth/login
│   ├── UserController.java          # GET /users
│   ├── ChatController.java          # GET /chats/direct, /chats/group/{id}
│   ├── GroupController.java         # CRUD /groups
│   └── WebSocketMessageController.java  # STOMP @MessageMapping handlers
├── service/
│   ├── AuthService.java             # Registration & login logic
│   ├── UserService.java             # User lookup & presence management
│   ├── MessageService.java          # Persist & retrieve messages
│   └── GroupService.java            # Group management
├── model/
│   ├── User.java, Message.java, Group.java
│   └── MessageType, MessageStatus, UserStatus (enums)
├── repository/                      # Spring Data MongoDB repositories
├── security/
│   ├── JwtTokenProvider.java        # JWT generation & validation
│   ├── JwtAuthFilter.java           # HTTP request JWT filter
│   ├── WebSecurityConfig.java       # Spring Security configuration
│   └── CustomUserDetails(Service).java
├── config/
│   ├── WebSocketConfig.java         # STOMP broker & endpoint config
│   ├── WebSocketAuthInterceptor.java # JWT validation on CONNECT frame
│   ├── WebSocketEventListener.java  # Online/offline presence + delivery
│   ├── CorsConfig.java             # CORS configuration for deployment
│   └── OpenApiConfig.java          # Swagger/OpenAPI metadata
└── exception/
    └── GlobalExceptionHandler.java  # Structured JSON error responses
```

---

## 📦 Data Models

### User
| Field | Type | Description |
|---|---|---|
| id | String | MongoDB ObjectId |
| name | String | Display name |
| email | String | Unique email (login) |
| password | String | BCrypt hashed |
| status | Enum | ONLINE / OFFLINE / LAST_SEEN |
| lastSeen | Instant | Timestamp of last disconnect |

### Message
| Field | Type | Description |
|---|---|---|
| id | String | MongoDB ObjectId |
| senderId | String | Sender user ID (indexed) |
| receiverId | String | Target user/group ID (indexed) |
| type | Enum | DIRECT / GROUP |
| content | String | Message text |
| timestamp | Instant | Send time (indexed) |
| status | Enum | SENT / DELIVERED / READ |

### Group
| Field | Type | Description |
|---|---|---|
| id | String | MongoDB ObjectId |
| name | String | Group display name |
| members | List\<String\> | User IDs |
| createdBy | String | Creator user ID |
| createdAt | Instant | Creation timestamp |

---

## 🛡️ Edge Cases Handled

| Edge Case | How It's Handled |
|---|---|
| User offline | Message persisted with status `SENT` |
| User reconnect | `markMessagesDelivered()` called on WebSocket connect |
| Message to non-existent user | Validated; error sent to sender via `/user/queue/errors` |
| Removed group member sending messages | Membership validated before broadcasting |
| Duplicate message delivery | Messages saved with unique MongoDB `_id` before broadcasting |
| Invalid JWT on WebSocket | `WebSocketAuthInterceptor` rejects CONNECT frame |

---

## 📬 Offline Message Delivery

Messages are **persisted to MongoDB before broadcasting**. If the recipient is offline:
1. The message is saved with status `SENT`
2. On reconnect, `WebSocketEventListener` calls `markMessagesDelivered(userId)`
3. Status upgraded to `DELIVERED` automatically
4. Client fetches missed messages via `GET /chats/direct`

---

## ⚡ Horizontal Scalability

The current implementation uses Spring's **in-memory STOMP broker**, which works on a single node.

To scale horizontally across multiple instances:
1. Replace the simple broker with **RabbitMQ** (STOMP plugin) or **Redis Pub/Sub**
2. Update `WebSocketConfig`:
   ```java
   // Replace enableSimpleBroker() with:
   registry.enableStompBrokerRelay("/topic", "/queue")
           .setRelayHost("rabbitmq-host")
           .setRelayPort(61613);
   ```
3. Deploy multiple Spring Boot instances behind a load balancer with **sticky sessions** OR configure a proper shared session store

---

## 🚢 Deployment (Render)

1. Push code to GitHub
2. Create a new **Web Service** on [Render](https://render.com) → connect repo
3. **Build command:** `./gradlew build -x test`
4. **Start command:** `java -jar build/libs/chat-backend-0.0.1-SNAPSHOT.jar`
5. Add environment variables: `MONGO_URI`, `JWT_SECRET`
6. Deploy and copy the live URL

---

## 🧪 Testing with curl

### Register
```bash
curl -X POST https://<your-app>/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@test.com","password":"pass123"}'
```

### Login
```bash
curl -X POST https://<your-app>/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"alice@test.com","password":"pass123"}'
```

### List Users (with token)
```bash
curl https://<your-app>/users \
  -H "Authorization: Bearer <token>"
```

---

## 📄 License

MIT License — free to use, modify, and distribute.
