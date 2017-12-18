package com.exsoinn.maven.plugin;

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.ArrayList;
import java.util.List;


/**
 *
 */
public abstract class AbstractCodeQualityMojo extends AbstractMojo {
    static final String MAVEN_SITE_PLUGIN = "maven-site-plugin";
    static final String MAVEN_SUREFIRE_PLUGIN = "maven-surefire-plugin";
    static final String MAVEN_GROUP = "org.apache.maven.plugins:";
    static final String MAVEN_SITE_PLUGIN_KEY = MAVEN_GROUP + MAVEN_SITE_PLUGIN;
    static final String MAVEN_SUREFIRE_PLUGIN_KEY = MAVEN_GROUP + MAVEN_SUREFIRE_PLUGIN;
    /**
     * The current  project that Maven is building
     */
    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    /**
     * The current Maven session
     */
    @Parameter (defaultValue = "${session}")
    private MavenSession mavenSession;


    @Component
    private BuildPluginManager pluginManager;


    /**
     *
     * @return - The name of this Mojo, child classes respnsuble for implementing
     */
    abstract String mojoName();

    DistributionManagement buildDistributionManagement(MavenProject pProj) {
        DistributionManagement dm = new DistributionManagement();
        pProj.setDistributionManagement(dm);
        return null;
    }

    String addMojoName(String pMsg) {
        return "[" + mojoName() + "]: " + pMsg;
    }


    /**
     * Format the info message according to our wants.
     * @param pMsg
     */
    void info(String pMsg) {
        getLog().info(addMojoName(pMsg));
    }


    Plugin createJacocoMavenPlugin() {
        return createMavenPugin("org.jacoco", "jacoco-maven-plugin", "0.7.9");
    }


    Plugin createJacocoReportMavenPlugin() {
        return createMavenPugin("org.jacoco", "jacoco-maven-plugin", "0.7.9");
    }


    Plugin createMavenPugin(String pGroupId, String pArtifactId, String pVersion) {
        return MojoExecutor.plugin(pGroupId, pArtifactId, pVersion);
    }



    ReportPlugin createMavenReportPugin(String pGroupId, String pArtifactId, String pVersion) {
        ReportPlugin rp = new ReportPlugin();
        rp.setGroupId(pGroupId);
        rp.setArtifactId(pArtifactId);
        rp.setVersion(pVersion);
        return rp;
    }


    /**
     *
     * @param pName
     * @param pVal
     * @param pTarget
     */
    void addSimpleTag(String pName, String pVal, Xpp3Dom pTarget) {
        Xpp3Dom tag = new Xpp3Dom(pName);
        tag.setValue(pVal);
        pTarget.addChild(tag);
    }


    boolean pluginExists(String pKey) {
        return project.getModel().getBuild().getPluginsAsMap().containsKey(pKey);
    }



    void addDependency(String pGroupId, String pArtifactId, String pVersion, Plugin pTargetPlugin) {
        Dependency d = new Dependency();
        d.setGroupId(pGroupId);
        d.setArtifactId(pArtifactId);
        d.setVersion(pVersion);
        List<Dependency> dependencies = new ArrayList<>();
        dependencies.addAll(pTargetPlugin.getDependencies());
        dependencies.add(d);
        pTargetPlugin.setDependencies(dependencies);
    }

    /*
     * Getters
     */
    public MavenProject getProject() {
        return project;
    }

    public MavenSession getMavenSession() {
        return mavenSession;
    }

    public BuildPluginManager getPluginManager() {
        return pluginManager;
    }
}
