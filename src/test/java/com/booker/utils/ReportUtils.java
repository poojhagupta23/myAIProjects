package com.booker.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ReportUtils — ExtentReports Manager (Singleton Pattern)
 *
 * Manages the lifecycle of the ExtentReports instance across the entire test suite.
 *
 * Design Decisions:
 * - Singleton via synchronized lazy initialization: ExtentReports must be created
 *   ONCE per suite run; multiple instances would result in fragmented/corrupt reports.
 * - ThreadLocal<ExtentTest>: Each test thread gets its own ExtentTest node.
 *   This is CRITICAL for parallel test execution safety — without ThreadLocal,
 *   concurrent tests would write to each other's report nodes.
 * - flush() is called by ExtentReportListener.onFinish() — never from within tests.
 *   This separation of concerns keeps test code clean.
 *
 * Report Output: test-output/ExtentReport.html
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public final class ReportUtils {

    private static final Logger log = LoggerFactory.getLogger(ReportUtils.class);

    /**
     * Singleton ExtentReports instance.
     * volatile ensures visibility across threads in a multi-threaded JVM.
     */
    private static volatile ExtentReports extentReports;

    /**
     * ThreadLocal ensures each test thread has its own ExtentTest node.
     * Prevents cross-test contamination in parallel execution.
     */
    private static final ThreadLocal<ExtentTest> extentTestThreadLocal = new ThreadLocal<>();

    // ── Private constructor: utility class ────────────────────────────────────
    private ReportUtils() {
        throw new UnsupportedOperationException("ReportUtils is a utility class.");
    }

    // ── ExtentReports Singleton ───────────────────────────────────────────────

    /**
     * Returns the singleton ExtentReports instance.
     * Uses double-checked locking for thread-safe lazy initialization.
     *
     * @return the shared ExtentReports instance
     */
    public static synchronized ExtentReports getExtentReports() {
        if (extentReports == null) {
            String reportOutputDir  = ConfigReader.getProperty("report.output.dir");
            String reportTitle      = ConfigReader.getProperty("report.title");
            String reportName       = ConfigReader.getProperty("report.name");
            String environment      = ConfigReader.getProperty("environment");
            String applicationName  = ConfigReader.getProperty("application.name");

            // Configure the HTML Spark reporter (dark theme, custom title)
            String reportPath = reportOutputDir + "/ExtentReport.html";
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            sparkReporter.config().setTheme(Theme.DARK);
            sparkReporter.config().setDocumentTitle(reportTitle);
            sparkReporter.config().setReportName(reportName);
            sparkReporter.config().setEncoding("UTF-8");

            // Initialize ExtentReports and attach the HTML reporter
            extentReports = new ExtentReports();
            extentReports.attachReporter(sparkReporter);

            // System info is displayed on the report dashboard
            extentReports.setSystemInfo("Framework",   "RestAssured + TestNG");
            extentReports.setSystemInfo("Application", applicationName);
            extentReports.setSystemInfo("Environment", environment);
            extentReports.setSystemInfo("Java Version", System.getProperty("java.version"));
            extentReports.setSystemInfo("OS", System.getProperty("os.name"));

            log.info("[ReportUtils] ExtentReports initialized. Report will be written to: {}", reportPath);
        }
        return extentReports;
    }

    // ── Test Node Management ──────────────────────────────────────────────────

    /**
     * Creates a new ExtentTest node for the current test and stores it in ThreadLocal.
     * Called by ExtentReportListener.onTestStart().
     *
     * @param testName    the test method name
     * @param description the test description (from @Test annotation)
     */
    public static void createTest(String testName, String description) {
        ExtentTest test = getExtentReports().createTest(testName, description);
        extentTestThreadLocal.set(test);
        log.debug("[ReportUtils] Created ExtentTest node for: {}", testName);
    }

    /**
     * Retrieves the ExtentTest node for the current thread.
     * Called from ExtentReportListener to log PASS/FAIL/SKIP status.
     *
     * @return the current thread's ExtentTest node
     */
    public static ExtentTest getTest() {
        return extentTestThreadLocal.get();
    }

    /**
     * Removes the current thread's ExtentTest from ThreadLocal.
     * Prevents memory leaks in long-running parallel executions.
     * Called by ExtentReportListener.onTestFinish().
     */
    public static void removeTest() {
        extentTestThreadLocal.remove();
    }

    /**
     * Flushes all test data to the HTML report file.
     * MUST be called at the end of the suite (not per-test) to ensure
     * all test results are written before the process exits.
     * Called by ExtentReportListener.onFinish().
     */
    public static synchronized void flush() {
        if (extentReports != null) {
            extentReports.flush();
            log.info("[ReportUtils] ExtentReports flushed successfully. HTML report is ready.");
        }
    }
}
