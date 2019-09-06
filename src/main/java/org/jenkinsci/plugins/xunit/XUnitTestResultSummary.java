package org.jenkinsci.plugins.xunit;

import org.jenkinsci.plugins.scriptsecurity.sandbox.whitelists.Whitelisted;
import hudson.tasks.junit.TestResultSummary;
import hudson.tasks.junit.TestResult;

import java.io.Serializable;

/**
 * Summary of test results that can be used in Pipeline scripts.
 */
public class XUnitTestResultSummary extends TestResultSummary {
    private String status;

    public XUnitTestResultSummary() {
        super();
    }

    public XUnitTestResultSummary(TestResult result, String status) {
        super(result);
        this.status = status;
    }

    @Whitelisted
    public String getStatus() { return status; }
}

