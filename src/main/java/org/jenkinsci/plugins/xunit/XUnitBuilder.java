/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014, Gregory Boissinot
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.jenkinsci.plugins.xunit;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.annotation.CheckForNull;

import org.jenkinsci.lib.dtkit.descriptor.TestTypeDescriptor;
import org.jenkinsci.lib.dtkit.type.TestType;
import org.jenkinsci.plugins.xunit.threshold.FailedThreshold;
import org.jenkinsci.plugins.xunit.threshold.SkippedThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThreshold;
import org.jenkinsci.plugins.xunit.threshold.XUnitThresholdDescriptor;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import hudson.DescriptorExtensionList;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Builder;
import hudson.tasks.junit.TestDataPublisher;
import jenkins.tasks.SimpleBuildStep;

/**
 * Class that converting custom reports to JUnit reports and records them.
 *
 * @author Gregory Boissinot
 * @deprecated Use {@link XUnitPublisher} instead of this.
 */
@Deprecated
public class XUnitBuilder extends Builder implements SimpleBuildStep {

    @XStreamAlias("types")
    private TestType[] tools;
    private XUnitThreshold[] thresholds;
    private int thresholdMode;
    private ExtraConfiguration extraConfiguration;

    @DataBoundConstructor
    public XUnitBuilder(@CheckForNull TestType[] tools, @CheckForNull XUnitThreshold[] thresholds, int thresholdMode, @CheckForNull String testTimeMargin) {
        this.tools = (tools != null ? Arrays.copyOf(tools, tools.length) : new TestType[0]);
        this.thresholds = (thresholds != null ? Arrays.copyOf(thresholds, thresholds.length) : new XUnitThreshold[0]);
        this.thresholdMode = thresholdMode;
        long longTestTimeMargin = XUnitUtil.parsePositiveLong(testTimeMargin, XUnitDefaultValues.TEST_REPORT_TIME_MARGING);
        this.extraConfiguration = new ExtraConfiguration(longTestTimeMargin, XUnitDefaultValues.JUNIT_FILE_REDUCE_LOG);
    }

    @DataBoundSetter
    public void setReduceLog(boolean reduceLog) {
        this.extraConfiguration = new ExtraConfiguration(this.extraConfiguration.getTestTimeMargin(), reduceLog);
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public boolean getReduceLog() {
        return extraConfiguration.isReduceLog();
    }

    /*
     * Needed to support Snippet Generator and Workflow properly.
     */
    public TestType[] getTools() {
        return tools;
    }

    /*
     * Needed to support Snippet Generator and Workflow properly
     */
    public String getTestTimeMargin() {
        return String.valueOf(getExtraConfiguration().getTestTimeMargin());
    }

    public XUnitThreshold[] getThresholds() {
        return thresholds;
    }

    public int getThresholdMode() {
        return thresholdMode;
    }

    public ExtraConfiguration getExtraConfiguration() {
        if (extraConfiguration == null) {
            extraConfiguration = new ExtraConfiguration(XUnitDefaultValues.TEST_REPORT_TIME_MARGING, XUnitDefaultValues.JUNIT_FILE_REDUCE_LOG);
        }
        return extraConfiguration;
    }

    @Override
    public void perform(final Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {
        listener.getLogger().println("WARNING: XUnitBuilder step is deprecated since 2.x, it has been replaced by XUnitPublisher. This builer will be remove in version 3.x");
        XUnitProcessor xUnitProcessor = new XUnitProcessor(getTools(), getThresholds(), getThresholdMode(), getExtraConfiguration());
        xUnitProcessor.process(build, workspace, listener, launcher, Collections.<TestDataPublisher> emptySet(), null);
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static final class XUnitDescriptorBuilder extends BuildStepDescriptor<Builder> {

        public XUnitDescriptorBuilder() {
            super(XUnitBuilder.class);
            load();
        }

        @Override
        public String getDisplayName() {
            return Messages.xUnit_BuilderName();
        }

        @Override
        public boolean isApplicable(@SuppressWarnings("rawtypes") Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public String getHelpFile() {
            return "/plugin/xunit/help.html";
        }

        public DescriptorExtensionList<TestType, TestTypeDescriptor<?>> getListXUnitTypeDescriptors() {
            return TestTypeDescriptor.all();
        }

        public DescriptorExtensionList<XUnitThreshold, XUnitThresholdDescriptor<?>> getListXUnitThresholdDescriptors() {
            return XUnitThresholdDescriptor.all();
        }

        public XUnitThreshold[] getListXUnitThresholdInstance() {
            return new XUnitThreshold[]{
                    new FailedThreshold(),
                    new SkippedThreshold()/*,
                    new PassedThreshold()*/
            };
        }
    }

}
