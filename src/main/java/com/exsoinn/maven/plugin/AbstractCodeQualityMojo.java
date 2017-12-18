package com.exsoinn.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;


/**
 *
 */
public abstract class AbstractCodeQualityMojo extends AbstractMojo {
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
     * @return
     */
    abstract String mojoName();


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
        pTargetPlugin.getDependencies().add(d);
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
