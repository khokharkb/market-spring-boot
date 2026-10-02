# 🛒 Market — Classified Ads Marketplace

A **marketplace** web application where users can buy, sell and trade items through classified ads. The project includes a **web application** (Spring Boot + Thymeleaf) and an **Android mobile app** (Capacitor) connected to a REST API.
🔗 **Live demo:**https://market-vogs.onrender.com *(the first load may take about a minute while the free server wakes up)*
![Home page](docs/screenshots/home.png)

---

## ✨ Features

**Users**
- 🔐 Secure registration and login (Spring Security, hashed passwords)
- 👤 Editable user profile

**Listings**
- 📝 Post ads with photos, price and description
- 🗂️ Browse by category (Home, Fashion, Health, Electronics, Other)
- 🔍 Search listings
- 📋 Manage your own listings ("My ads")

**Interaction**
- ❤️ Favorites
- 🛍️ Shopping cart
- 💬 Messaging between buyers and sellers
- ⭐ Rating system

**Administration**
- 📊 Admin dashboard

**Mobile**
- 📱 Android app (Capacitor) using a dedicated REST API

---

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, Spring Data JPA (Hibernate) |
| Web frontend | Thymeleaf, HTML, CSS |
| Database | MySQL |
| Mobile | Capacitor (Android), JavaScript |
| Tools | Maven, Lombok, Git |

---

## 📸 Screenshots

| Home | Recent listings |
|---|---|
| ![Home](docs/screenshots/home.png) | ![Listings](docs/screenshots/annonces.png) |

> The interface is in French.

---

## 🚀 Running Locally

### Prerequisites
- Java 21
- MySQL 8
- (Optional, for mobile) Node.js and Android Studio

### 1. Clone the repository
```bash
git clone https://github.com/khokharkb/market-spring-boot.git
cd market-spring-boot
```

### 2. Configure the database
The `market` database is created automatically on startup.
If your MySQL server doesn't run on port `3307`, update the URL in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/market?createDatabaseIfNotExist=true
```

### 3. Set environment variables
Passwords are not stored in the code:

| Variable | Description |
|---|---|
| `DB_PASSWORD` | MySQL password |
| `DB_URL` | *(optional)* JDBC URL, defaults to the local MySQL database |
| `DB_USERNAME` | *(optional)* MySQL user, defaults to `root` |
| `ADMIN_PASSWORD` | Password of the `admin` account, created on first startup |
| `ADMIN_EMAIL` | *(optional)* Email of the `admin` account |

In IntelliJ: *Run → Edit Configurations → Environment variables*.

### 4. Start the application
```bash
./mvnw spring-boot:run
```
Then open **http://localhost:8080**

### 5. (Optional) Mobile app
```bash
npm install
npx cap sync android
npx cap open android
```

---

## 📁 Project Structure

```
src/main/java/org/example/market/
├── admin/        # Admin dashboard
├── annonce/      # Listings and image uploads
├── api/mobile/   # REST API for the mobile app
├── favoris/      # Favorites
├── message/      # Messaging
├── panier/       # Shopping cart
├── rating/       # Ratings
├── security/     # Authentication and security
└── user/         # Users and profiles
```

---

## 👩‍💻 Author

**Khouloud** — [GitHub](https://github.com/khokharkb)