# 📘 Restful Booker API Automation Framework — Documentation

> **Framework Type:** Enterprise-grade REST API Test Automation  
> **Version:** 1.0.0  
> **Author:** QA Automation Team  
> **Last Updated:** 31 May 2026  
> **Status:** ✅ All 10 Tests Passing

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [Technology Stack](#2-technology-stack)
3. [Project Structure](#3-project-structure)
4. [Architecture — 2-Layer API Object Model](#4-architecture--2-layer-api-object-model)
5. [Setup & Installation](#5-setup--installation)
6. [Configuration](#6-configuration)
7. [Test Data Management](#7-test-data-management)
8. [Framework Components](#8-framework-components)
   - [Layer 2 — BookingEndpoints.java](#81-layer-2--bookingendpointsjava)
   - [Layer 1 — BookingApiClient.java](#82-layer-1--bookingapiclientjava)
   - [Models (POJOs)](#83-models-pojos)
   - [ConfigReader.java](#84-configreaderjava)
   - [TestDataLoader.java](#85-testdataloaderjava)
   - [ReportUtils.java](#86-reportutilsjava)
   - [ExtentReportListener.java](#87-extentreportlistenerjava)
   - [BaseTest.java](#88-basetestjava)
9. [Test Cases](#9-test-cases)
   - [CreateBookingTest.java](#91-createbookingtestjava)
   - [GetBookingTest.java](#92-getbookingtestjava)
   - [E2EBookingFlowTest.java](#93-e2ebookingflowtestjava)
10. [APIs Under Test](#10-apis-under-test)
11. [Running the Tests](#11-running-the-tests)
12. [Reports](#12-reports)
13. [Design Principles & Decisions](#13-design-principles--decisions)
14. [Extending the Framework](#14-extending-the-framework)
15. [Troubleshooting](#15-troubleshooting)
16. [Test Execution Results](#16-test-execution-results)

---

## 1. Project Overview

This is a **production-ready, enterprise-grade REST API automation framework** built for testing the [Restful Booker API](https://restful-booker.herokuapp.com). It is designed to be:

- **Zero hardcoded** — every URL, value, and configuration is externalized
- **Scalable** — adding new APIs or tests requires minimal changes
- **Readable** — clean code with Builder patterns, descriptive test names, and inline documentation
- **CI/CD Ready** — runs with a single `mvn clean test` command on any machine

### What It Tests

The framework validates two core API operations:

| Operation | Endpoint | Description |
|-----------|----------|-------------|
| **Create Booking** | `POST /booking` | Creates a new hotel room reservation |
| **Get Booking** | `GET /booking/{id}` | Retrieves a reservation by its unique ID |

These two APIs are connected via an **End-to-End flow**: create a booking → capture its ID → retrieve and validate it.

---

## 2. Technology Stack

| Tool / Library | Version | Purpose |
|----------------|---------|---------|
| **Java** | 11 (LTS) | Core programming language |
| **Maven** | 3.9.6 | Build tool, dependency management |
| **RestAssured** | 5.3.2 | REST API HTTP client and assertion library |
| **TestNG** | 7.9.0 | Test framework (annotations, lifecycle, listeners) |
| **Allure TestNG** | 2.25.0 | Advanced test reporting with steps and attachments |
| **Allure RestAssured** | 2.25.0 | Captures HTTP request/response automatically in Allure |
| **ExtentReports** | 5.1.1 | HTML dashboard-style test report |
| **Jackson Databind** | 2.16.0 | JSON serialization and deserialization (POJO ↔ JSON) |
| **AspectJ Weaver** | 1.9.21 | Required for Allure `@Step` annotation instrumentation |
| **SLF4J + Logback** | 2.0.9 / 1.4.14 | Structured logging to console and rolling file |

> **Why TestNG over JUnit?**  
> TestNG provides richer lifecycle annotations (`@BeforeSuite`, `@BeforeClass`, `@BeforeMethod`), built-in parallel execution via `thread-count`, listener support via `@Listeners`, and native `testng.xml` suite management — all critical for enterprise-scale API automation.

---

## 3. Project Structure

```
restful-booker-automation/
│
├── pom.xml                          ← Maven build config + all dependency versions
├── testng.xml                       ← TestNG suite runner (defines test groups & order)
├── allure.properties                ← Root-level Allure results directory config
├── README.md                        ← Quick-start guide
├── DOCUMENTATION.md                 ← This file
│
└── src/
    └── test/
        ├── java/
        │   └── com/
        │       └── booker/
        │           │
        │           ├── api/                         ← 2-LAYER API OBJECT MODEL
        │           │   ├── BookingEndpoints.java    ← Layer 2: endpoint path constants
        │           │   └── BookingApiClient.java    ← Layer 1: RestAssured HTTP calls
        │           │
        │           ├── models/                      ← POJO data models
        │           │   ├── BookingDates.java        ← Check-in / checkout dates object
        │           │   ├── BookingRequest.java      ← Full booking request payload
        │           │   └── BookingResponse.java     ← Create booking API response
        │           │
        │           ├── utils/                       ← Utility / helper classes
        │           │   ├── ConfigReader.java        ← Reads config.properties (fail-fast)
        │           │   ├── TestDataLoader.java      ← Loads JSON test data from resources
        │           │   └── ReportUtils.java         ← ExtentReports singleton manager
        │           │
        │           ├── listeners/
        │           │   └── ExtentReportListener.java ← TestNG → ExtentReports bridge
        │           │
        │           ├── base/
        │           │   └── BaseTest.java            ← Abstract parent for all test classes
        │           │
        │           └── tests/                       ← THE 3 TEST FILES
        │               ├── CreateBookingTest.java   ← 5 tests: POST /booking
        │               ├── GetBookingTest.java      ← 4 tests: GET /booking/{id}
        │               └── E2EBookingFlowTest.java  ← 1 E2E: Create → Get → Validate
        │
        └── resources/
            ├── config.properties                    ← All environment config (no hardcoding)
            ├── allure.properties                    ← Allure results dir (classpath)
            ├── logback-test.xml                     ← Console + rolling file logging config
            └── testdata/
                └── booking.json                     ← Valid booking payload (test data)
```

---

## 4. Architecture — 2-Layer API Object Model

The framework implements a **two-layer API Object Model**, adapted from the Page Object Model (POM) pattern used in UI automation.

```
┌─────────────────────────────────────────────────────────────────┐
│                        TEST LAYER                               │
│  CreateBookingTest  │  GetBookingTest  │  E2EBookingFlowTest    │
│                                                                 │
│  Responsibilities:                                              │
│  - TestNG lifecycle (@BeforeClass, @Test)                       │
│  - Allure annotations (@Feature, @Story, @Severity)            │
│  - Call API client methods                                      │
│  - Assert response values using TestNG Assert                   │
└────────────────────────────┬────────────────────────────────────┘
                             │  calls methods on
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   LAYER 1 — BookingApiClient                    │
│                                                                 │
│  Responsibilities:                                              │
│  - Own ALL RestAssured code                                     │
│  - Build and execute HTTP requests (GET, POST)                  │
│  - Apply Allure filter for request/response capture             │
│  - Return raw Response objects to the test layer               │
│  - Pre-serialize bodies when needed (e.g. non-JSON content)     │
└────────────────────────────┬────────────────────────────────────┘
                             │  references paths from
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                   LAYER 2 — BookingEndpoints                    │
│                                                                 │
│  Responsibilities:                                              │
│  - Store all endpoint path constants (ONLY)                     │
│  - No HTTP logic, no imports from RestAssured                   │
│  - Single source of truth for API paths                         │
│                                                                 │
│  Constants:                                                     │
│  CREATE_BOOKING       = "/booking"                              │
│  GET_BOOKING_BY_ID    = "/booking/{bookingId}"                  │
│  GET_ALL_BOOKINGS     = "/booking"                              │
│  UPDATE_BOOKING       = "/booking/{bookingId}"                  │
│  DELETE_BOOKING       = "/booking/{bookingId}"                  │
│  AUTH_TOKEN           = "/auth"                                 │
└─────────────────────────────────────────────────────────────────┘
```

### Why Two Layers?

| Problem without layering | Solution with 2-Layer model |
|--------------------------|----------------------------|
| If `/booking` changes to `/v2/booking`, you edit every test | Update only `BookingEndpoints.java` |
| Test classes contain RestAssured code — hard to maintain | `BookingApiClient` owns all HTTP logic |
| Duplicate request setup across tests | Shared `baseRequestSpec` in `BookingApiClient` |
| Tests hard to read | Test classes only contain assertions and test logic |

---

## 5. Setup & Installation

### Prerequisites

| Tool | Install Command (macOS) | Verify |
|------|------------------------|--------|
| Java 11 | `brew install --cask temurin@11` | `java -version` |
| Maven 3.8+ | `brew install maven` | `mvn -version` |
| Allure CLI *(optional)* | `brew install allure` | `allure --version` |

> **Note:** If Homebrew is not installed:  
> `/bin/bash -c "$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)"`

### Without Package Manager (Manual)

If you cannot install via Homebrew (no admin rights), you can download directly:

```bash
# Create a local tools directory
mkdir -p ~/dev-tools

# Download Java 11 (ARM Mac / Apple Silicon)
curl -L "https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.23%2B9/OpenJDK11U-jdk_aarch64_mac_hotspot_11.0.23_9.tar.gz" \
  -o ~/dev-tools/jdk11.tar.gz
cd ~/dev-tools && tar -xzf jdk11.tar.gz

# Download Maven
curl -L "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.tar.gz" \
  -o ~/dev-tools/maven.tar.gz
cd ~/dev-tools && tar -xzf maven.tar.gz

# Set environment variables
export JAVA_HOME=~/dev-tools/jdk-11.0.23+9/Contents/Home
export PATH=$JAVA_HOME/bin:~/dev-tools/apache-maven-3.9.6/bin:$PATH
```

### Clone / Navigate to Project

```bash
cd "/Users/poojhagupta/Documents/AI Automation Project-1/restful-booker-automation"
```

---

## 6. Configuration

All configuration is centralized in one file — **no value is hardcoded in any Java class**.

### `src/test/resources/config.properties`

```properties
# ── Environment ──────────────────────────────────────────
environment=QA
application.name=Restful Booker API

# ── Base URL ─────────────────────────────────────────────
base.url=https://restful-booker.herokuapp.com

# ── HTTP Headers ─────────────────────────────────────────
content.type=application/json
accept.type=application/json

# ── Test Data File References ─────────────────────────────
testdata.valid.booking=booking.json

# ── Negative Test IDs ─────────────────────────────────────
invalid.booking.id=999999999
boundary.booking.id=0

# ── Expected HTTP Status Codes ────────────────────────────
status.code.ok=200
status.code.not.found=404
status.code.server.error=500

# ── Report Settings ───────────────────────────────────────
report.output.dir=test-output
report.title=Restful Booker API — Test Execution Report
report.name=API Automation Report
```

### How to Change Environments

To point the framework at a different environment (e.g. staging), simply update `base.url`:

```properties
base.url=https://staging.restful-booker.com
```

No Java code changes needed.

---

## 7. Test Data Management

### `src/test/resources/testdata/booking.json`

```json
{
  "firstname": "Pooja",
  "lastname": "Gupta",
  "totalprice": 500,
  "depositpaid": true,
  "bookingdates": {
    "checkin": "2026-01-01",
    "checkout": "2026-01-10"
  },
  "additionalneeds": "Breakfast"
}
```

### How Test Data Flows

```
booking.json
    │
    │  loaded by
    ▼
TestDataLoader.loadBookingRequest("booking.json")
    │
    │  returns
    ▼
BookingRequest (POJO)
    │
    │  passed to
    ▼
BookingApiClient.createBooking(bookingRequest)
    │
    │  serialized to JSON by Jackson + RestAssured
    ▼
POST https://restful-booker.herokuapp.com/booking
```

### Adding New Test Data Files

1. Create a new JSON file in `src/test/resources/testdata/` (e.g. `booking_premium.json`)
2. Add a reference in `config.properties`: `testdata.premium.booking=booking_premium.json`
3. Load it in your test: `TestDataLoader.loadBookingRequest(ConfigReader.getProperty("testdata.premium.booking"))`

---

## 8. Framework Components

### 8.1 Layer 2 — `BookingEndpoints.java`

**Package:** `com.booker.api`  
**Purpose:** Stores all API endpoint paths as constants. Contains **zero HTTP logic**.

```java
public final class BookingEndpoints {
    public static final String CREATE_BOOKING    = "/booking";
    public static final String GET_BOOKING_BY_ID = "/booking/{bookingId}";
    public static final String GET_ALL_BOOKINGS  = "/booking";
    public static final String UPDATE_BOOKING    = "/booking/{bookingId}";
    public static final String DELETE_BOOKING    = "/booking/{bookingId}";
    public static final String AUTH_TOKEN        = "/auth";
}
```

**Design Decisions:**
- `final` class with private constructor — cannot be instantiated or extended
- If any API path changes, only this file needs updating — not a single test class

---

### 8.2 Layer 1 — `BookingApiClient.java`

**Package:** `com.booker.api`  
**Purpose:** Executes all HTTP calls using RestAssured. Test classes never call RestAssured directly.

**Key Methods:**

| Method | Description |
|--------|-------------|
| `createBooking(BookingRequest)` | POST /booking with a valid POJO payload |
| `createBookingWithRawBody(String)` | POST /booking with raw JSON string (negative tests) |
| `createBookingWithContentType(BookingRequest, String)` | POST /booking with a custom Content-Type header |
| `getBookingById(int)` | GET /booking/{bookingId} by ID |

**Important Design Decisions:**

1. **Shared `baseRequestSpec`** — built once in constructor, reused in all methods. Ensures consistent headers without duplication.
2. **`AllureRestAssured` filter** — applied globally. Every request/response is auto-captured in Allure reports.
3. **`createBookingWithContentType` pre-serializes to String** — when Content-Type is not JSON (e.g. `text/plain`), RestAssured cannot serialize a POJO. Jackson's `ObjectMapper` converts it to a JSON string first, then it's sent as a raw String body.
4. **Path parameters via `.pathParam()`** — never via string concatenation (`"/booking/" + id`). This is safer, more readable, and aligns with RestAssured best practices.

---

### 8.3 Models (POJOs)

**Package:** `com.booker.models`

All models use **Jackson annotations** for JSON serialization and the **Builder pattern** for clean object construction.

#### `BookingRequest.java`

Maps to the POST /booking request body:

```json
{
  "firstname":      "Pooja",
  "lastname":       "Gupta",
  "totalprice":     500,
  "depositpaid":    true,
  "bookingdates":   { "checkin": "2026-01-01", "checkout": "2026-01-10" },
  "additionalneeds": "Breakfast"
}
```

#### `BookingDates.java`

Nested object within `BookingRequest`:

```json
{ "checkin": "2026-01-01", "checkout": "2026-01-10" }
```

#### `BookingResponse.java`

Maps to the POST /booking response:

```json
{
  "bookingid": 2271,
  "booking":   { ...full BookingRequest... }
}
```

**Why `@JsonIgnoreProperties(ignoreUnknown = true)`?**  
If the API adds new fields in the future, deserialization will not break — the new fields are simply ignored. This is forward compatibility by design.

**Why Builder Pattern?**  
Enables fluent, readable construction:

```java
BookingRequest request = BookingRequest.builder()
    .firstname("Pooja")
    .lastname("Gupta")
    .totalprice(500)
    .depositpaid(true)
    .bookingdates(BookingDates.builder()
        .checkin("2026-01-01")
        .checkout("2026-01-10")
        .build())
    .additionalneeds("Breakfast")
    .build();
```

---

### 8.4 `ConfigReader.java`

**Package:** `com.booker.utils`  
**Purpose:** Thread-safe, fail-fast loader for `config.properties`.

**Key Design Decisions:**
- **Static initializer** loads properties once at class-load time — no repeated I/O during test execution
- **Fail-fast** — throws a descriptive `RuntimeException` if any key is missing. Surfaces misconfiguration immediately, not mid-test with a `NullPointerException`
- **Thread-safe reads** — static final Properties object with no mutation after initialization

```java
// Usage anywhere in the framework:
String baseUrl = ConfigReader.getProperty("base.url");
int statusOk   = ConfigReader.getIntProperty("status.code.ok");
```

---

### 8.5 `TestDataLoader.java`

**Package:** `com.booker.utils`  
**Purpose:** Loads JSON test data files from `src/test/resources/testdata/` and deserializes them into POJO instances.

**Key Design Decisions:**
- **Static `ObjectMapper`** — Jackson's `ObjectMapper` is thread-safe for reading. Creating it once is the recommended best practice.
- **`ClassLoader.getResourceAsStream()`** — Classpath-relative paths work identically on any OS, any CI environment, without needing absolute paths.

```java
// Usage:
BookingRequest booking = TestDataLoader.loadBookingRequest("booking.json");
String rawJson         = TestDataLoader.loadRawJson("booking.json");
```

---

### 8.6 `ReportUtils.java`

**Package:** `com.booker.utils`  
**Purpose:** Manages the `ExtentReports` singleton and per-thread `ExtentTest` nodes.

**Key Design Decisions:**
- **Singleton with double-checked locking** — `ExtentReports` must be created once per run. Multiple instances would create fragmented HTML files.
- **`ThreadLocal<ExtentTest>`** — each test thread gets its own `ExtentTest` node. **Critical for parallel execution safety.** Without `ThreadLocal`, concurrent tests would write to each other's report entries.
- **`flush()` called only in `onFinish()`** — never inside individual tests. Prevents partial writes and ensures all test data is written before the report is closed.

---

### 8.7 `ExtentReportListener.java`

**Package:** `com.booker.listeners`  
**Purpose:** Bridges TestNG's test lifecycle events to ExtentReports.

| TestNG Event | Action |
|-------------|--------|
| `onTestStart` | Creates a new `ExtentTest` node (via `ReportUtils.createTest()`) |
| `onTestSuccess` | Logs `PASS` status with ✅ |
| `onTestFailure` | Logs `FAIL` status + full stack trace |
| `onTestSkipped` | Logs `SKIP` status with ⏭️ |
| `onFinish` | Calls `ReportUtils.flush()` to write HTML to disk |

**Why registered on `BaseTest` via `@Listeners` and not in `testng.xml`?**  
Registering on `BaseTest` makes the dependency explicit and IDE-discoverable. Any class that extends `BaseTest` automatically gets the listener — no need to remember to add it to `testng.xml`.

---

### 8.8 `BaseTest.java`

**Package:** `com.booker.base`  
**Purpose:** Abstract parent class for all test classes. Provides shared lifecycle hooks and the listener registration.

```java
@Listeners(ExtentReportListener.class)  // ← auto-applies to all subclasses
public abstract class BaseTest {

    @BeforeSuite(alwaysRun = true)      // ← runs ONCE before entire suite
    public void globalSetup() { ... }

    @AfterSuite(alwaysRun = true)       // ← runs ONCE after entire suite
    public void globalTearDown() { ... }

    protected BookingApiClient createApiClient() { ... }  // ← factory method
}
```

**Why `abstract`?**  
Prevents direct instantiation of `BaseTest`. It is purely a structural parent — it should never be run as a test class itself.

---

## 9. Test Cases

### 9.1 `CreateBookingTest.java`

**API:** `POST https://restful-booker.herokuapp.com/booking`

| Test ID | Description | Type | Expected Result |
|---------|-------------|------|----------------|
| `TC_CREATE_001` | Submit a complete, valid booking — verify success and booking ID | ✅ Positive | HTTP 200 + `bookingid > 0` |
| `TC_CREATE_002` | Submit a valid booking — verify response contains exact same data | ✅ Positive | HTTP 200 + all fields match |
| `TC_CREATE_003` | Submit an empty request body `{}` | ❌ Negative | HTTP 500 |
| `TC_CREATE_004` | Submit a booking with `firstname` field missing | ❌ Negative | Not HTTP 200 |
| `TC_CREATE_005` | Submit with `Content-Type: text/plain` instead of JSON | ❌ Negative | Not HTTP 200 |

**Setup:** `@BeforeClass` loads valid booking data from `booking.json` via `TestDataLoader`.

---

### 9.2 `GetBookingTest.java`

**API:** `GET https://restful-booker.herokuapp.com/booking/{bookingId}`

| Test ID | Description | Type | Expected Result |
|---------|-------------|------|----------------|
| `TC_GET_001` | Retrieve an existing booking by its valid ID | ✅ Positive | HTTP 200 |
| `TC_GET_002` | Retrieve a booking and verify all fields match the original creation data | ✅ Positive | HTTP 200 + all fields match |
| `TC_GET_003` | Request a non-existent booking ID (999999999) | ❌ Negative | HTTP 404 Not Found |
| `TC_GET_004` | Request booking with ID = 0 (boundary value) | ❌ Negative | Not HTTP 200 |

**Key Setup Decision:**  
`@BeforeClass` creates a **real booking** via `POST /booking` to obtain a valid `bookingId`. This ensures GET tests always use a freshly-created, guaranteed-to-exist booking — no reliance on pre-existing data or hardcoded IDs.

---

### 9.3 `E2EBookingFlowTest.java`

**Flow:** `POST /booking` → capture `bookingId` → `GET /booking/{id}` → validate

| Test ID | Description | Type |
|---------|-------------|------|
| `TC_E2E_001` | Create a booking → capture its ID → retrieve it → verify every single field matches | 🔄 E2E |

**4-Step Breakdown:**

```
STEP 1 — POST /booking
         Sends: { firstname: "Pooja", lastname: "Gupta", totalprice: 500, ... }
         Asserts: HTTP 200 + bookingid is a positive integer

STEP 2 — Capture bookingId
         Deserializes response to BookingResponse
         Stores: capturedBookingId = 2271  (varies per run)

STEP 3 — GET /booking/2271
         Asserts: HTTP 200

STEP 4 — Validate all fields
         Compares: firstname, lastname, totalprice, depositpaid,
                   additionalneeds, checkin, checkout
         All must match the original POST request exactly
```

**Why self-contained?**  
`E2EBookingFlowTest` creates its own booking — it does NOT depend on `CreateBookingTest` or `GetBookingTest` being run first. This ensures test isolation and prevents cascading failures.

---

## 10. APIs Under Test

### API 1 — Create Booking

```
POST https://restful-booker.herokuapp.com/booking
Content-Type: application/json
Accept: application/json

Request Body:
{
    "firstname":       "Pooja",
    "lastname":        "Gupta",
    "totalprice":      500,
    "depositpaid":     true,
    "bookingdates": {
        "checkin":     "2026-01-01",
        "checkout":    "2026-01-10"
    },
    "additionalneeds": "Breakfast"
}

Success Response — HTTP 200:
{
    "bookingid": 2271,
    "booking": {
        "firstname":       "Pooja",
        "lastname":        "Gupta",
        "totalprice":      500,
        "depositpaid":     true,
        "bookingdates": {
            "checkin":     "2026-01-01",
            "checkout":    "2026-01-10"
        },
        "additionalneeds": "Breakfast"
    }
}
```

### API 2 — Get Booking

```
GET https://restful-booker.herokuapp.com/booking/2271
Accept: application/json

Success Response — HTTP 200:
{
    "firstname":       "Pooja",
    "lastname":        "Gupta",
    "totalprice":      500,
    "depositpaid":     true,
    "bookingdates": {
        "checkin":     "2026-01-01",
        "checkout":    "2026-01-10"
    },
    "additionalneeds": "Breakfast"
}

Not Found Response — HTTP 404:
"Not Found"
```

---

## 11. Running the Tests

### Run Full Suite

```bash
mvn clean test
```

### Run Specific Groups Only

```bash
# Only smoke tests (fastest — 3 tests)
mvn clean test -Dgroups=smoke

# Only regression tests (all 10 tests)
mvn clean test -Dgroups=regression

# Only E2E tests
mvn clean test -Dgroups=e2e

# Only negative tests
mvn clean test -Dgroups=negative
```

### Run a Single Test Class

```bash
mvn clean test -Dtest=CreateBookingTest
mvn clean test -Dtest=GetBookingTest
mvn clean test -Dtest=E2EBookingFlowTest
```

### Run a Single Test Method

```bash
mvn clean test -Dtest=CreateBookingTest#tc_create_001_validPayload_returns200WithBookingId
```

### Skip Tests (compile only)

```bash
mvn clean test-compile -DskipTests
```

---

## 12. Reports

### ExtentReports — HTML Dashboard

Auto-generated after every `mvn clean test` run.

```bash
# Open in browser
open test-output/ExtentReport.html
```

Features:
- Dark-themed dashboard
- Pass/Fail/Skip summary
- Per-test log output
- System information panel
- Stack traces for failures

### Allure Report — Advanced Reporting

```bash
# Generate the Allure report
mvn allure:report

# Open in browser
open target/site/allure-maven-plugin/allure-maven.html

# Serve interactively (requires Allure CLI)
allure serve target/allure-results
```

Features:
- HTTP request/response captured per test (via `AllureRestAssured` filter)
- `@Step` annotations create a step-by-step execution breakdown
- `@Feature`, `@Story`, `@Epic` annotations create a BDD-style test hierarchy
- Timeline view, trend charts (when run multiple times)

### TestNG Native Surefire Report

```bash
open target/surefire-reports/index.html
```

### Human-Readable Report

```bash
open test-output/TestReport.html
```
Custom-built HTML report with plain-English test descriptions, no technical jargon.

### Report Locations Summary

| Report | Location |
|--------|----------|
| ExtentReports HTML | `test-output/ExtentReport.html` |
| Human-Readable HTML | `test-output/TestReport.html` |
| Allure HTML | `target/site/allure-maven-plugin/allure-maven.html` |
| Allure Raw Results | `target/allure-results/` |
| TestNG Surefire | `target/surefire-reports/index.html` |
| Execution Logs | `test-output/logs/automation.log` |

---

## 13. Design Principles & Decisions

### ① Zero Hardcoding

Every configurable value lives in `config.properties` or `testdata/booking.json`. Test classes contain **only logic and assertions** — never raw URLs, status codes, or test data strings.

```java
// ❌ WRONG — hardcoded
Assert.assertEquals(response.getStatusCode(), 200);

// ✅ CORRECT — from config
Assert.assertEquals(response.getStatusCode(), ConfigReader.getIntProperty("status.code.ok"));
```

### ② Fail-Fast Configuration

`ConfigReader` throws a clear `RuntimeException` if any property key is missing. This surfaces misconfiguration **at suite startup** rather than mid-test:

```
[ConfigReader] Property key 'base.url' not found in config.properties.
Please verify the key exists and has a non-empty value.
```

### ③ ThreadLocal for Parallel Safety

`ReportUtils.extentTestThreadLocal` stores one `ExtentTest` per thread. This allows parallel test classes (as configured in `testng.xml`) to write to their own report nodes without interference.

### ④ Descriptive Assertion Messages

Every `Assert` call includes a descriptive failure message:

```java
Assert.assertEquals(
    returnedBooking.getFirstname(),
    validBookingRequest.getFirstname(),
    "TC_GET_002 FAILED: 'firstname' mismatch — Expected: "
    + validBookingRequest.getFirstname()
    + ", Got: " + returnedBooking.getFirstname()
);
```

This makes CI failure logs immediately actionable without needing to dig into stack traces.

### ⑤ Separation of Concerns

| Layer | What it knows | What it doesn't know |
|-------|--------------|---------------------|
| `BookingEndpoints` | API paths | HTTP calls, tests, assertions |
| `BookingApiClient` | HTTP calls | Test logic, assertions, reporting |
| `ConfigReader` | config.properties | Tests, APIs, reports |
| `TestDataLoader` | JSON files | Tests, APIs, HTTP |
| `ReportUtils` | ExtentReports | Tests, APIs, config |
| Test classes | Assertions, test flow | RestAssured, report internals |

---

## 14. Extending the Framework

### Adding a New API Endpoint

**Step 1 — Add path constant to `BookingEndpoints.java`:**
```java
public static final String UPDATE_BOOKING = "/booking/{bookingId}";
```

**Step 2 — Add method to `BookingApiClient.java`:**
```java
@Step("PUT /booking/{bookingId} — Update booking: {bookingId}")
public Response updateBooking(int bookingId, BookingRequest updatedRequest) {
    return given()
        .spec(baseRequestSpec)
        .header("Cookie", "token=" + authToken)
        .pathParam("bookingId", bookingId)
        .body(updatedRequest)
        .when()
        .put(BookingEndpoints.UPDATE_BOOKING)
        .then().log().all()
        .extract().response();
}
```

**Step 3 — Create a new test class extending `BaseTest`:**
```java
public class UpdateBookingTest extends BaseTest {
    @Test
    public void tc_update_001_validUpdate_returns200() { ... }
}
```

**Step 4 — Add it to `testng.xml`:**
```xml
<class name="com.booker.tests.UpdateBookingTest"/>
```

### Adding a New Test Data File

```bash
# Create the file
touch src/test/resources/testdata/booking_vip.json
```

```json
{
  "firstname": "VIP",
  "lastname": "Guest",
  "totalprice": 5000,
  "depositpaid": true,
  "bookingdates": { "checkin": "2026-06-01", "checkout": "2026-06-07" },
  "additionalneeds": "Airport Pickup, Suite"
}
```

```properties
# config.properties
testdata.vip.booking=booking_vip.json
```

```java
// In your test
BookingRequest vipBooking = TestDataLoader.loadBookingRequest(
    ConfigReader.getProperty("testdata.vip.booking")
);
```

---

## 15. Troubleshooting

### `command not found: mvn`

Maven is not on your PATH. Run:
```bash
export PATH=~/dev-tools/apache-maven-3.9.6/bin:$PATH
```

### `Unable to locate a Java Runtime`

Java is not installed or not on PATH. Run:
```bash
export JAVA_HOME=~/dev-tools/jdk-11.0.23+9/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH
```

### `config.properties not found`

Ensure the file exists at `src/test/resources/config.properties`. It must be in the classpath — Maven's `test-compile` phase handles this automatically.

### `Cannot serialize because cannot determine how to serialize content-type text/plain`

This happens when you pass a POJO as body with a non-JSON Content-Type. **Already fixed** in `BookingApiClient.createBookingWithContentType()` — it pre-serializes using `ObjectMapper.writeValueAsString()`.

### Tests fail with `Connection refused`

The Restful Booker API (`https://restful-booker.herokuapp.com`) may be temporarily down (it's a free Heroku demo). Wait 30 seconds and retry. If it's still down, check [https://restful-booker.herokuapp.com/booking](https://restful-booker.herokuapp.com/booking) in a browser.

### Allure report is empty

Ensure `target/allure-results/` contains `.json` result files after `mvn clean test`. Then run `mvn allure:report`. If `allure-results` is empty, the AspectJ weaver javaagent may not have been applied — check the `pom.xml` Surefire plugin configuration.

---

## 16. Test Execution Results

### Latest Run — 31 May 2026

| Metric | Value |
|--------|-------|
| **Total Tests** | 10 |
| **Passed** | 10 ✅ |
| **Failed** | 0 |
| **Skipped** | 0 |
| **Total Duration** | 23.560 seconds |
| **Suite Start** | 19:25:07 IST |
| **Environment** | QA |
| **Base URL** | https://restful-booker.herokuapp.com |
| **E2E Booking ID** | 2271 |

### Individual Test Results

| # | Test ID | Test Description | Result | Duration |
|---|---------|-----------------|--------|---------|
| 1 | TC_CREATE_001 | Valid booking → HTTP 200 + Booking ID returned | ✅ PASS | ~1.8s |
| 2 | TC_CREATE_002 | Response body matches the submitted booking data | ✅ PASS | ~1.6s |
| 3 | TC_CREATE_003 | Empty request body → server error returned | ✅ PASS | ~1.2s |
| 4 | TC_CREATE_004 | Missing first name → not accepted | ✅ PASS | ~1.1s |
| 5 | TC_CREATE_005 | Wrong data format (text/plain) → rejected | ✅ PASS | ~0.9s |
| 6 | TC_GET_001 | Retrieve existing booking → HTTP 200 | ✅ PASS | ~1.4s |
| 7 | TC_GET_002 | Retrieved booking matches original data exactly | ✅ PASS | ~1.5s |
| 8 | TC_GET_003 | Non-existent booking ID → HTTP 404 | ✅ PASS | ~1.3s |
| 9 | TC_GET_004 | Booking ID = 0 (boundary) → not found | ✅ PASS | ~1.1s |
| 10 | TC_E2E_001 | Create → Capture ID `2271` → Retrieve → All fields match | ✅ PASS | ~3.5s |

### Bug Fixed During Execution

| # | Issue | Root Cause | Fix Applied |
|---|-------|-----------|------------|
| 1 | `TC_CREATE_005` failed on first run | RestAssured cannot serialize a Java POJO when `Content-Type: text/plain` — no registered serializer for that MIME type | Pre-serialize POJO → JSON string using `Jackson ObjectMapper.writeValueAsString()`, then pass as raw `String` body. RestAssured handles `String` bodies for any content type. |

---

*Documentation generated for: Restful Booker API Automation Framework v1.0.0*  
*Framework built with: RestAssured 5.3.2 · TestNG 7.9.0 · Java 11 · Maven 3.9.6*
