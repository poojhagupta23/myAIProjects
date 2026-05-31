package com.booker.listeners;

import com.aventstack.extentreports.Status;
import com.booker.utils.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * ExtentReportListener — TestNG ITestListener for ExtentReports Integration
 *
 * Hooks into the TestNG test lifecycle to automatically:
 * - Create ExtentTest nodes when tests start
 * - Log PASS / FAIL / SKIP statuses
 * - Capture failure stack traces
 * - Flush the report when the suite finishes
 *
 * Design Decisions:
 * - Implements ITestListener (not ISuiteListener) for per-test granularity.
 * - Registered via @Listeners annotation on BaseTest — not in testng.xml —
 *   so it applies automatically to all classes that extend BaseTest.
 * - Stack trace is attached to FAIL log so developers can diagnose failures
 *   directly from the HTML report without opening the console log.
 * - ReportUtils.removeTest() is called in onTestFinish to prevent ThreadLocal
 *   memory leaks in parallel execution environments.
 *
 * @author QA Automation Framework
 * @version 1.0.0
 */
public class ExtentReportListener implements ITestListener {

    private static final Logger log = LoggerFactory.getLogger(ExtentReportListener.class);

    /**
     * Invoked each time a test method starts.
     * Creates a new ExtentTest node bound to the current thread.
     */
    @Override
    public void onTestStart(ITestResult result) {
        String testName    = result.getMethod().getMethodName();
        String description = result.getMethod().getDescription();
        log.info("[Listener] Test STARTED: {}", testName);
        ReportUtils.createTest(testName, description != null ? description : "");
    }

    /**
     * Invoked when a test method passes.
     * Logs a PASS status to the current ExtentTest node.
     */
    @Override
    public void onTestSuccess(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.info("[Listener] Test PASSED: {}", testName);
        ReportUtils.getTest().log(Status.PASS, "✅ PASSED: " + testName);
        ReportUtils.removeTest();
    }

    /**
     * Invoked when a test method fails.
     * Logs a FAIL status with the full exception message and stack trace.
     */
    @Override
    public void onTestFailure(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        Throwable throwable = result.getThrowable();
        log.error("[Listener] Test FAILED: {} — {}", testName, throwable != null ? throwable.getMessage() : "Unknown error");

        if (ReportUtils.getTest() != null) {
            ReportUtils.getTest().log(Status.FAIL, "❌ FAILED: " + testName);
            if (throwable != null) {
                // Log full stack trace so the HTML report is self-sufficient for debugging
                ReportUtils.getTest().log(Status.FAIL, throwable);
            }
        }
        ReportUtils.removeTest();
    }

    /**
     * Invoked when a test method is skipped (e.g., due to a failed dependency).
     * Logs a SKIP status.
     */
    @Override
    public void onTestSkipped(ITestResult result) {
        String testName = result.getMethod().getMethodName();
        log.warn("[Listener] Test SKIPPED: {}", testName);

        if (ReportUtils.getTest() != null) {
            ReportUtils.getTest().log(Status.SKIP, "⏭️ SKIPPED: " + testName);
        }
        ReportUtils.removeTest();
    }

    /**
     * Invoked when all tests in the context finish.
     * Flushes the ExtentReports to write the HTML file to disk.
     * This is the ONLY place flush() should be called.
     */
    @Override
    public void onFinish(ITestContext context) {
        log.info("[Listener] Suite finished. Flushing ExtentReports...");
        ReportUtils.flush();
        log.info("[Listener] ✅ ExtentReport HTML written to: test-output/ExtentReport.html");
    }
}
