# 🧪 Restful Booker API Automation Framework

> Enterprise-grade REST API automation framework built with **RestAssured + Java + Maven + TestNG**
> **2-Layer API Object Model · Dual Reporting (Allure + ExtentReports) · Zero Hardcoding**

---

## 📁 Project Structure

```
restful-booker-automation/
├── pom.xml                              ← Maven config + all dependencies
├── testng.xml                           ← TestNG suite runner
├── allure.properties                    ← Allure config
└── src/test/
    ├── java/com/booker/
    │   ├── api/
    │   │   ├── BookingEndpoints.java    ← LAYER 2: Endpoint path constants
    │   │   └── BookingApiClient.java    ← LAYER 1: RestAssured HTTP client
    │   ├── models/
    │   │   ├── BookingDates.java        ← POJO: Booking dates
    │   │   ├── BookingRequest.java      ← POJO: Request payload
    │   │   └── BookingResponse.java     ← POJO: API response
    │   ├── utils/
    │   │   ├── ConfigReader.java        ← Properties file reader
    │   │   ├── TestDataLoader.java      ← JSON test data loader
    │   │   └── ReportUtils.java        ← ExtentReports manager
    │   ├── listeners/
    │   │   └── ExtentReportListener.java ← TestNG → ExtentReports hook
    │   ├── base/
    │   │   └── BaseTest.java            ← Abstract base for all tests
    │   └── tests/
    │       ├── CreateBookingTest.java   ← 5 tests: POST /booking
    │       ├── GetBookingTest.java      ← 4 tests: GET /booking/{id}
    │       └── E2EBookingFlowTest.java  ← 1 E2E flow test
    └── resources/
        ├── config.properties            ← All config values (no hardcoding)
        ├── allure.properties            ← Allure results dir
        ├── logback-test.xml             ← Logging config
        └── testdata/
            └── booking.json            ← Valid booking test data
```

---

## ⚙️ Prerequisites

| Tool      | Version       | Install |
|-----------|--------------|---------|
| **Java**  | JDK 11+      | [Download](https://adoptium.net/) or `brew install temurin@11` |
| **Maven** | 3.8+         | `brew install maven` |
| **Allure CLI** | 2.x (optional) | `brew install allure` |

---

## 🚀 Quick Start

### 1. Install Prerequisites (macOS)
```bash
# Install Java 11
brew install --cask temurin@11

# Install Maven
brew install maven

# Verify installations
java -version
mvn -version
```

### 2. Run All Tests
```bash
cd "restful-booker-automation"

# Run the full suite
mvn clean test

# Run only smoke tests
mvn clean test -Dgroups=smoke

# Run only E2E tests
mvn clean test -Dgroups=e2e

# Run only regression tests (includes negative)
mvn clean test -Dgroups=regression
```

### 3. View Reports

#### 📊 ExtentReports (HTML — auto-generated)
```bash
open test-output/ExtentReport.html
```

#### 📈 Allure Report
```bash
# Generate Allure report
mvn allure:report

# Open in browser
open target/site/allure-maven-plugin/index.html

# OR serve interactively
allure serve target/allure-results
```

#### 📋 TestNG Native Report
```bash
open target/surefire-reports/index.html
```

---

## 🧩 Test Case Matrix

| Test ID | Test Class | Description | Type |
|---------|------------|-------------|------|
| TC_CREATE_001 | CreateBookingTest | Valid payload → HTTP 200 + bookingid | ✅ Positive |
| TC_CREATE_002 | CreateBookingTest | Response body fields match request | ✅ Positive |
| TC_CREATE_003 | CreateBookingTest | Empty body → HTTP 500 | ❌ Negative |
| TC_CREATE_004 | CreateBookingTest | Missing firstname → error | ❌ Negative |
| TC_CREATE_005 | CreateBookingTest | Invalid Content-Type → error | ❌ Negative |
| TC_GET_001 | GetBookingTest | Valid ID → HTTP 200 | ✅ Positive |
| TC_GET_002 | GetBookingTest | Response matches original data | ✅ Positive |
| TC_GET_003 | GetBookingTest | Non-existent ID → HTTP 404 | ❌ Negative |
| TC_GET_004 | GetBookingTest | Boundary ID (0) → HTTP 404 | ❌ Negative |
| TC_E2E_001 | E2EBookingFlowTest | Create → Capture ID → Get → Validate | 🔄 E2E |

**Total: 10 test cases · 5 positive · 4 negative · 1 E2E**

---

## 🏗️ Architecture: 2-Layer API Object Model

```
Test Classes (CreateBookingTest / GetBookingTest / E2EBookingFlowTest)
      │
      │ calls
      ▼
BookingApiClient.java  ←── LAYER 1: RestAssured HTTP calls
      │
      │ references paths from
      ▼
BookingEndpoints.java  ←── LAYER 2: Endpoint path constants (no HTTP logic)
```

- **Layer 2 (BookingEndpoints)**: Pure constants — endpoint paths only. If an API path changes, update only this file.
- **Layer 1 (BookingApiClient)**: All RestAssured calls. Tests never use RestAssured directly.

---

## 📦 Tech Stack

| Component | Library | Version |
|-----------|---------|---------|
| REST API Testing | RestAssured | 5.3.2 |
| Test Framework | TestNG | 7.9.0 |
| HTML Reports | ExtentReports | 5.1.1 |
| Allure Reports | allure-testng | 2.25.0 |
| JSON Parsing | Jackson | 2.16.0 |
| Logging | Logback | 1.4.14 |

---

## 📝 Configuration

All configurable values are in **`src/test/resources/config.properties`**:

```properties
base.url=https://restful-booker.herokuapp.com
content.type=application/json
testdata.valid.booking=booking.json
invalid.booking.id=999999999
status.code.ok=200
status.code.not.found=404
```

> ⚠️ **Never hardcode** values in test classes. Always use `ConfigReader.getProperty("key")`.

---

## 🎯 Design Principles

- ✅ Zero hardcoded values — everything from config/testdata
- ✅ TestNG only — no JUnit dependencies
- ✅ 2-Layer API Object Model — clean separation of concerns
- ✅ Parallel execution ready — ThreadLocal in ReportUtils
- ✅ Dual reporting — Allure + ExtentReports HTML
- ✅ Fail-fast configuration — descriptive errors on misconfiguration
- ✅ CI/CD ready — `mvn clean test` works on any machine with Java + Maven
