package com.booker.base;

import com.booker.api.BookingApiClient;
import com.booker.listeners.ExtentReportListener;
import com.booker.utils.ConfigReader;
import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

/**
 * BaseTest — Abstract Base Class for All Test Classes
 *
 * Provides:
 * 1. Common @BeforeSuite setup (RestAssured base configuration)
 * 2. Common @AfterSuite teardown
 * 3. Listener registration via @Listeners annotation
 * 4. A shared BookingApiClient instance (lazily created per subclass via getter)
 *
 * Design Decisions:
 * - @Listeners registered HERE on BaseTest rather than on each test class.
 *   This ensures ExtentReportListener is ALWAYS active for any class that extends BaseTest.
 *   Adding it to testng.xml is another option, but class-level registration
 *   makes the dependency explicit and IDE-discoverable.
 *
 * - @BeforeSuite runs ONCE before the entire suite — ideal for global setup
 *   like RestAssured configuration, which should not repeat per test.
 *
 * - @BeforeClass is left for individual test classes to instantiate their
 *   own BookingApiClient — this keeps initialization scoped to where it's needed.
 *
 * - The class is abstract to enforce that it's only used as a base class,
 *   never instantiated directly.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
@Listeners(ExtentReportListener.class)
public abstract class BaseTest {

    protected static final Logger log = LoggerFactory.getLogger(BaseTest.class);

    /**
     * Suite-level setup — runs ONCE before all tests.
     * Configures RestAssured global defaults from config.properties.
     */
    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        log.info("══════════════════════════════════════════════════");
        log.info("  🚀 STARTING: Restful Booker API Automation Suite");
        log.info("  Environment : {}", ConfigReader.getProperty("environment"));
        log.info("  Base URL    : {}", ConfigReader.getProperty("base.url"));
        log.info("══════════════════════════════════════════════════");

        // Set RestAssured global base URI — applies to all tests in the suite
        RestAssured.baseURI = ConfigReader.getProperty("base.url");

        // Disable RestAssured URL encoding if the API has specific requirements
        // RestAssured.urlEncodingEnabled = false;
    }

    /**
     * Suite-level teardown — runs ONCE after all tests complete.
     * Useful for cleanup, DB resets, or final logging.
     */
    @AfterSuite(alwaysRun = true)
    public void globalTearDown() {
        log.info("══════════════════════════════════════════════════");
        log.info("  ✅ COMPLETED: Restful Booker API Automation Suite");
        log.info("  Reports: test-output/ExtentReport.html");
        log.info("  Allure:  target/allure-results/ (run: mvn allure:report)");
        log.info("══════════════════════════════════════════════════");
    }

    /**
     * Factory method for creating a BookingApiClient.
     * Subclasses call this in @BeforeClass to get their client instance.
     *
     * @return a new BookingApiClient configured from config.properties
     */
    protected BookingApiClient createApiClient() {
        return new BookingApiClient();
    }
}
