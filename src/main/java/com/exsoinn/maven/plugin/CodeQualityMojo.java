package com.exsoinn.maven.plugin;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;


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
@Mojo(name = "code-quality-init", defaultPhase = LifecyclePhase.INITIALIZE)
public class CodeQualityMojo extends AbstractMojo {
    static final String JACOCO_ARG_LINE_PROP_NAME = "jacocoArgLine";
    /**
     * Tuen current project that Maven is building
     */
    @Parameter(defaultValue = "${project}")
    private MavenProject project;


    /**
     * The current Maven session
     */
    @Parameter (defaultValue = "${session}")
    private MavenSession mavenSession;


    /**
     * This is required. It gives the location of the classes against which code quality
     * check will run.
     */
    @Parameter(required = true)
    private String packageName;


    @Component
    private BuildPluginManager pluginManager;

    /**
     *
     * @throws MojoExecutionException
     * @throws MojoFailureException
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("Code quality checks will be run against this package: " + packageName);
        Plugin jacocoPlugin = createJacocoMavenPlugin();
        Xpp3Dom configuration = MojoExecutor.configuration();
        configureJacocoMavenPlugin(configuration, packageName);
        executeMojo(jacocoPlugin, "prepare-agent", configuration, executionEnvironment(project,
                mavenSession, pluginManager));
        getLog().info("The jacoco prepare-agent goal defined the following project property: "
                + project.getProperties().getProperty(JACOCO_ARG_LINE_PROP_NAME));
    }



    private Plugin createJacocoMavenPlugin() {
        Plugin plugin = new Plugin();
        plugin.setArtifactId("jacoco-maven-plugin");
        plugin.setGroupId("org.jacoco");
        plugin.setVersion("0.7.9");
        return plugin;
    }



    private void configureJacocoMavenPlugin(Xpp3Dom pConfig, String pPkgName) {
        Xpp3Dom propNameTag = new Xpp3Dom("propertyName");
        propNameTag.setValue(JACOCO_ARG_LINE_PROP_NAME);
        pConfig.addChild(propNameTag);

        Xpp3Dom includesParentNode = new Xpp3Dom("includes");

        Xpp3Dom includesChildNode = new Xpp3Dom("include");
        includesChildNode.setValue(pPkgName);
        includesParentNode.addChild(includesChildNode);
    }
}
