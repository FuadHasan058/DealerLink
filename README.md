# DealerLink — Smart B2B Shop–Dealer Platform

DealerLink is a JavaFX desktop procurement application that connects local retail shop owners with commercial product distributors using SQLite persistence and RESTful Open-Meteo weather intelligence.

---

## Features

* **Shop Owners:** Broadcast supply requests, compare competing dealer quotations, place confirmed purchase orders, and monitor shipments.
* **Dealers:** Manage warehouse stock levels and pricing, submit competitive bids on open requests, and update transit checkpoints with dispatch statuses (`SHIPPED`, `DELIVERED`).
* **Concurrency:** Background database operations and REST calls execute off the main thread via cached thread pools (`SessionManager.executor()`), keeping the JavaFX UI responsive.
* **Weather Intelligence:** Automatically fetches real-time route weather and temperatures using Open-Meteo REST APIs and Jackson JSON parsing to aid dispatch decisions.
* **Local Persistence:** Automated SQLite schema generation with foreign key enforcement (`PRAGMA foreign_keys = ON;`).

---

## Tech Stack

* **Language:** Java 17+
* **GUI:** JavaFX 21 (FXML + CSS)
* **Database:** SQLite via JDBC
* **Networking & JSON:** Java 11 `HttpClient` & Jackson Databind
* **Security:** SHA-256 password hashing

---

## Project Structure

```text
DealerLink/
├── pom.xml
└── src/main/
    ├── java/com/dealerlink/
    │   ├── App.java                      # Main entry point & scene switcher
    │   ├── controller/                   # Login, Shop & Dealer UI controllers
    │   ├── dao/                          # User, Product, Inventory, Request, Quotation, Order, Delivery DAOs
    │   ├── db/DatabaseManager.java       # SQLite schema & seed setup
    │   ├── model/                        # Entity models
    │   ├── service/WeatherService.java   # HTTP REST client & Jackson JSON parser
    │   └── util/                         # Thread pools (SessionManager) & PasswordUtil
    └── resources/com/dealerlink/
        ├── css/style.css                 # SaaS desktop stylesheet
        └── fxml/                         # Login, ShopDashboard, DealerDashboard views

```

---

## Quick Start

### 1. Build

```bash
mvn clean compile

```

### 2. Run

```bash
mvn javafx:run

```

*(Or run `App.main()` directly in IntelliJ).*

---

## Workflow Overview

1. **Register:** Create both a **Shop** and a **Dealer** account on the initial screen.
2. **Add Inventory:** Dealer logs in, enters warehouse stock (e.g., *Cement*, *bag*, *500*, *$8.50*), and saves.
3. **Submit Request:** Shop owner logs in and broadcasts a request for desired items and quantities.
4. **Quote Bid:** Dealer inspects the request in **Open Market Requests** and submits a price per unit and lead time.
5. **Order & Dispatch:** Shop owner accepts the quotation to create an order. The dealer updates dispatch progress with transit checkpoints, pulling live route weather data automatically.
