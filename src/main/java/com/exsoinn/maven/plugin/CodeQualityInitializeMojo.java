package com.exsoinn.maven.plugin;

import org.apache.maven.model.*;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;
import java.util.ArrayList;
import java.util.List;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;


/**
 * Mojo that encapsulates all logic associated with the init steps to effectuate the
 * code quality check.
 *
 * TODO: See if some of this can be moved to abstract class, because there will be more Mojo classes to handle
 * TODO:  other goals
 * TODO: Once abstract class is created, make final package private constansts for things like the plugin names
 * TODO: Externalize certain things, like artifact version. Think about it, if there's a bug fix in one of
 * TODO:  the Maven projects we depend on, do we want to be updating the Java for that? For now hardcoded.
 */
@Mojo(name = "code-quality-initialize", defaultPhase = LifecyclePhase.INITIALIZE)
public class CodeQualityInitializeMojo extends AbstractCodeQualityMojo{
    static final String JACOCO_ARG_LINE_PROP_NAME = "jacocoArgLine";
    static final List<String> reports;

    static {
        reports = new ArrayList<>();
        reports.add("dependencies");
        reports.add("plugins");
        reports.add("license");
        reports.add("index");
        reports.add("dependency-convergence");
        reports.add("cim");
        reports.add("dependency-info");
        reports.add("dependency-management");
        reports.add("distribution-management");
        reports.add("help");
        reports.add("issue-tracking");
        reports.add("mailing-list");
        reports.add("modules");
        reports.add("plugin-management");
        reports.add("plugins");
        reports.add("project-team");
        reports.add("scm");
        reports.add("summary");
    }

    /**
     * This is required. It gives the location of the classes against which code quality
     * check will run.
     */
    @Parameter(required = true)
    private String packageName;


    /**
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        info("Jacoco will be run against this package: " + packageName);
        Plugin jacocoPlugin = createJacocoMavenPlugin();
        Xpp3Dom configuration = MojoExecutor.configuration();
        configureJacocoMavenPluginForInitialize(configuration, packageName);
        executeMojo(jacocoPlugin, "prepare-agent", configuration, executionEnvironment(getProject(),
                getMavenSession(), getPluginManager()));

        info("The jacoco prepare-agent goal defined the following project property: "
                + getProject().getProperties().getProperty(JACOCO_ARG_LINE_PROP_NAME));
        autoConfigureMavenSitePlugin(getProject());
        info("Adding plugins to reporting section...");
        addReportingPlugins(getProject().getModel().getReporting());
        info("Done adding plugins to reporting section.");
    }


    /**
     * Auto-configures the "maven-site-plugin" to suit our needs, creating it first if not already
     * present, else dependencies are cleared and added from scratch.
     * @param pProject
     */
    void autoConfigureMavenSitePlugin(MavenProject pProject ) {
        getLog().info("Re-configuring maven-site-plugin...");
        Plugin p;
        if (null == (p = pProject.getBuild().getPluginsAsMap().get("maven-site-plugin"))) {
            p = plugin("org.apache.maven.plugins", "maven-site-plugin", "3.6");
        }
        p.getDependencies().clear();
        addDependency("org.apache.maven.skins", "maven-fluido-skin", "1.6", p);
        addDependency("org.apache.maven.doxia", "doxia-module-markdown", "1.7", p);
        getLog().info("Done re-configuring maven-site-plugin.");
    }


    /**
     * Dynamically add reporting plugins. We do this reasonably early enough so that they're
     * present during "mvn site" life-cycle later on. The assumption is that this Mojo will be invoked
     * in a phase earlier then the site phase, otherwise all bets are off.
     * TODO: For quick test purposes do here, might need to move to its own class.
     *
     * @param pReportingNode
     */
    private void addReportingPlugins(Reporting pReportingNode) {
        ReportPlugin reportsPlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-project-info-reports-plugin", "2.9");
        pReportingNode.addPlugin(reportsPlugin);
        ReportSet rptSet = new ReportSet();
        reportsPlugin.addReportSet(rptSet);
        for (String r : reports) {
            rptSet.addReport(r);
        }


        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-surefire-report-plugin", "${maven.surefire.plugin.version}"));
        pReportingNode.addPlugin(createMavenReportPugin("org.jacoco", "jacoco-maven-plugin", "0.7.9"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "codenarc-maven-plugin", "0.22-1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "findbugs-maven-plugin", "3.0.4"));

        /*
         *
         */
        ReportPlugin checkStylePlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-checkstyle-plugin", "${maven.checkstyle.plugin.version}");
        pReportingNode.addPlugin(checkStylePlugin);
        Xpp3Dom checkstyleConfig = MojoExecutor.configuration();
        checkStylePlugin.setConfiguration(checkstyleConfig);
        addSimpleTag("configLocation", "google_checks.xml", checkstyleConfig);


        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "jdepend-maven-plugin", "2.0"));
        pReportingNode.addPlugin(createMavenReportPugin("org.owasp", "dependency-check-maven", "3.0.2"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-dependency-plugin", "${maven.dependency.plugin.version}"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-plugin-plugin", "3.5"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-jxr-plugin", "2.5"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-javadoc-plugin", "3.0.0-M1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-changelog-plugin", "2.3"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.plexus",
                "plexus-component-metadata", "1.7.1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.basepom.maven",
                "duplicate-finder-maven-plugin", "1.2.1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-enforcer-plugin", "0.7.9"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "license-maven-plugin", ""));


        /*
         * Prepare the "maven-pmd-plugin" plugin
         */
        ReportPlugin pmdPlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-pmd-plugin", "${maven.pmd.plugin.version}");
        pReportingNode.addPlugin(pmdPlugin);
        Xpp3Dom pmdConfig = MojoExecutor.configuration();
        pmdPlugin.setConfiguration(pmdConfig);
        addSimpleTag("linkXref", "true", pmdConfig);
        addSimpleTag("sourceEncoding", "utf-8", pmdConfig);
        addSimpleTag("minimumTokens", "100", pmdConfig);
        addSimpleTag("targetJdk", "${jdk.version}", pmdConfig);
        Xpp3Dom excludesParentNode = new Xpp3Dom("excludes");
        pmdConfig.addChild(excludesParentNode);
        addSimpleTag("exclude", "**/*Test.java", excludesParentNode);
        addSimpleTag("exclude", "**/generated/*.java", excludesParentNode);
        Xpp3Dom excludeRootsNode = new Xpp3Dom("excludeRoots");
        pmdConfig.addChild(excludeRootsNode);
        addSimpleTag("excludeRoot", "target/generated-sources/stubs", excludeRootsNode);
        addSimpleTag("skipEmptyReport", "false", pmdConfig);


        ReportPlugin versionsMavenPlugin = createMavenReportPugin("org.codehaus.mojo",
                "versions-maven-plugin", "2.5");
        pReportingNode.addPlugin(versionsMavenPlugin);
        ReportSet vmpRptSet = new ReportSet();
        vmpRptSet.addReport("dependency-updates-report");
        vmpRptSet.addReport("plugin-updates-report");
        vmpRptSet.addReport("property-updates-report");
        versionsMavenPlugin.addReportSet(vmpRptSet);
    }


    private void configureJacocoMavenPluginForInitialize(Xpp3Dom pConfig, String pPkgName) {
        Xpp3Dom propNameTag = new Xpp3Dom("propertyName");
        propNameTag.setValue(JACOCO_ARG_LINE_PROP_NAME);
        pConfig.addChild(propNameTag);
        Xpp3Dom includesParentNode = new Xpp3Dom("includes");
        Xpp3Dom includesChildNode = new Xpp3Dom("include");
        includesChildNode.setValue(pPkgName);
        includesParentNode.addChild(includesChildNode);
        pConfig.addChild(includesParentNode);
    }

    @Override
    String mojoName() {
        return "code-quality-initialize";
    }
}
