package com.exsoinn.maven.plugin;


import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

@Mojo(name = "code-quality-site", defaultPhase = LifecyclePhase.PRE_SITE)
public class CodeQualitySiteMojo extends AbstractCodeQualityMojo {
    private static final List<String> reports;


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

    @Override
    String mojoName() {
        return "code-quality-site";
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        autoConfigureMavenSitePlugin(getProject());
        addReportingPlugins(getProject().getModel().getReporting());

        Plugin mavenSitePlugin = autoConfigureMavenSitePlugin(getProject());
        //executeMojo(mavenSitePlugin, "pre-site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
                //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
        executeMojo(mavenSitePlugin, "site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
                executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
        //executeMojo(mavenSitePlugin, "post-site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
                //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));

        // Get rid of config, not needed for "deploy" goal
        //mavenSitePlugin.setConfiguration(MojoExecutor.configuration());
        //executeMojo(mavenSitePlugin, "deploy", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
                //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
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
        info("Adding plugins to reporting section...");
        ReportPlugin reportsPlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-project-info-reports-plugin", "2.9");
        pReportingNode.addPlugin(reportsPlugin);
        ReportSet rptSet = new ReportSet();
        reportsPlugin.addReportSet(rptSet);
        for (String r : reports) {
            rptSet.addReport(r);
        }


        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-surefire-report-plugin", "2.19.1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.jacoco", "jacoco-maven-plugin", "0.7.9"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "codenarc-maven-plugin", "0.22-1"));
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "findbugs-maven-plugin", "3.0.4"));

        /*
         *
         */
        ReportPlugin checkStylePlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-checkstyle-plugin", "2.17");
        pReportingNode.addPlugin(checkStylePlugin);
        Xpp3Dom checkstyleConfig = MojoExecutor.configuration();
        checkStylePlugin.setConfiguration(checkstyleConfig);
        addSimpleTag("configLocation", "google_checks.xml", checkstyleConfig);


        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "jdepend-maven-plugin", "2.0"));
        pReportingNode.addPlugin(createMavenReportPugin("org.owasp", "dependency-check-maven", "3.0.2"));
        pReportingNode.addPlugin(createMavenReportPugin("org.apache.maven.plugins",
                "maven-dependency-plugin", "2.10"));
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
        pReportingNode.addPlugin(createMavenReportPugin("org.codehaus.mojo", "license-maven-plugin", "1.14"));


        /*
         * Prepare the "maven-pmd-plugin" plugin
         */
        ReportPlugin pmdPlugin = createMavenReportPugin("org.apache.maven.plugins",
                "maven-pmd-plugin", "3.8");
        pReportingNode.addPlugin(pmdPlugin);
        Xpp3Dom pmdConfig = MojoExecutor.configuration();
        pmdPlugin.setConfiguration(pmdConfig);
        addSimpleTag("linkXref", "true", pmdConfig);
        addSimpleTag("sourceEncoding", "utf-8", pmdConfig);
        addSimpleTag("minimumTokens", "100", pmdConfig);
        addSimpleTag("targetJdk", "1.8", pmdConfig);
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
        info("Done adding plugins to reporting section. New POM model is:");
    }


    /**
     * Auto-configures the {@link this#MAVEN_SITE_PLUGIN} to suit our needs, creating it first if not already
     * present, else dependencies are cleared and added from scratch. Why are we doing this here? Because
     * we need this done before "site" phase. This Mojo is tied to "initialize" phase. Later we may
     * refactor things better if there's room for improvement.
     * @param pProject
     */
    Plugin autoConfigureMavenSitePlugin(MavenProject pProject ) {
        final String mspVersion = "3.6";
        getLog().info("Re-configuring " + MAVEN_SITE_PLUGIN + "...");
        Plugin p;
        if (null == (p = pProject.getBuild().getPluginsAsMap().get(MAVEN_SITE_PLUGIN_KEY))) {
            p = plugin("org.apache.maven.plugins", MAVEN_SITE_PLUGIN, mspVersion);
            pProject.getModel().getBuild().getPluginsAsMap().put(MAVEN_SITE_PLUGIN_KEY, p);
        }
        p.setVersion("3.6");
        //getPluginManager().loadPlugin();
        p.getDependencies().clear();
        addDependency("org.apache.maven.skins", "maven-fluido-skin", "1.6", p);
        addDependency("org.apache.maven.doxia", "doxia-module-markdown", "1.7", p);
        Xpp3Dom conf = MojoExecutor.configuration();
        p.setConfiguration(conf);
        addSimpleTag("inputEncoding", "UTF-8", conf);
        addSimpleTag("outputEncoding", "UTF-8", conf);

        Map<String, Artifact> pluginArtifactMap = pProject.getPluginArtifactMap();
        Artifact msp = pluginArtifactMap.get(MAVEN_SITE_PLUGIN_KEY);
        msp.setVersion(mspVersion);
        getLog().info("Done re-configuring " + MAVEN_SITE_PLUGIN + ".");
        return p;
    }
}
