package com.exsoinn.maven.plugin;


import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.ArrayList;
import java.util.List;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

@Mojo(name = "code-quality-test", defaultPhase = LifecyclePhase.TEST)
public class CodeQualityTestMojo extends AbstractCodeQualityMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        info("Running test phase");
        //Plugin msfp = autoConfigureMavenSurefirePlugin(getProject());
        //executeMojo(msfp, "test", createMavenSureFireConfigurationForTest(),
          //      executionEnvironment(getProject(),
             //   getMavenSession(), getPluginManager()));

        Plugin jacocoPlugin = createJacocoMavenPlugin();
        Xpp3Dom configuration = MojoExecutor.configuration();
        // For Jacoco no configuration necessary for its goal "report", at least
        // not for our purposes.
        executeMojo(jacocoPlugin, "report", configuration, executionEnvironment(getProject(),
                getMavenSession(), getPluginManager()));

        info("Done running test phase.");
    }


    Xpp3Dom createMavenSureFireConfigurationForTest() {
        Xpp3Dom conf = MojoExecutor.configuration();
        addSimpleTag("argLine", "${jacocoArgLine} -Xmx256m", conf);
        return conf;
    }

    Plugin autoConfigureMavenSurefirePlugin(MavenProject pProject ) {
        getLog().info("Re-configuring " + MAVEN_SUREFIRE_PLUGIN + "...");
        Plugin p;
        //if (null == (p = pProject.getBuild().getPluginsAsMap().get(MAVEN_SUREFIRE_PLUGIN_KEY))) {
        p = plugin("org.apache.maven.plugins", MAVEN_SUREFIRE_PLUGIN, "2.19.1");
        pProject.getModel().getBuild().getPluginsAsMap().put(MAVEN_SUREFIRE_PLUGIN_KEY, p);
        //}
        p.getDependencies().clear();
        //addDependency("org.junit.platform", "junit-platform-surefire-provider", "1.0.1", p);
        //addDependency("org.junit.jupiter", "junit-jupiter-engine", "5.0.1", p);
        Xpp3Dom conf = MojoExecutor.configuration();
        p.setConfiguration(conf);
        addSimpleTag("argLine", "${jacocoArgLine} -Xmx256m", conf);

        PluginExecution pe = new PluginExecution();
        pe.setId("default-test");
        List<PluginExecution> peList = new ArrayList<>();
        peList.add(pe);
        p.setExecutions(peList);
        getLog().info("Done re-configuring " + MAVEN_SITE_PLUGIN + ".");
        return p;
    }



    @Override
    String mojoName() {
        return "code-quality-test";
    }
}
