package com.exsoinn.maven.plugin;


import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

@Mojo(name = "code-quality-test", defaultPhase = LifecyclePhase.TEST)
public class CodeQualityTestMojo extends AbstractCodeQualityMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        info("Running test phase");
        Plugin jacocoPlugin = createJacocoMavenPlugin();
        Xpp3Dom configuration = MojoExecutor.configuration();
        // For Jacoco no configuration necessary for its goal "report", at least
        // not for our purposes.
        executeMojo(jacocoPlugin, "report", configuration, executionEnvironment(getProject(),
                getMavenSession(), getPluginManager()));

        info("Done running test phase.");
    }


    @Override
    String mojoName() {
        return "code-quality-test";
    }
}
