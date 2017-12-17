package com.exsoinn.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;


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
        Plugin plugin = new Plugin();
        plugin.setArtifactId("jacoco-maven-plugin");
        plugin.setGroupId("org.jacoco");
        plugin.setVersion("0.7.9");
        return plugin;
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
