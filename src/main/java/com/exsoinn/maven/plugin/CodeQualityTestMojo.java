package com.exsoinn.maven.plugin;


import static com.exsoinn.maven.plugin.CodeQualityHelper.MAVEN_SITE_PLUGIN;
import static com.exsoinn.maven.plugin.CodeQualityHelper.MAVEN_SUREFIRE_PLUGIN;
import static com.exsoinn.maven.plugin.CodeQualityHelper.MAVEN_SUREFIRE_PLUGIN_KEY;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

@Mojo(name = "code-quality-test", defaultPhase = LifecyclePhase.TEST)
public class CodeQualityTestMojo extends AbstractCodeQualityMojo {

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    info("Running test phase");
    //Plugin msfp = autoConfigureMavenSurefirePlugin(getProject());
    //executeMojo(msfp, "test", createMavenSureFireConfigurationForTest(),
    //      executionEnvironment(getProject(),
    //   getMavenSession(), getPluginManager()));

    Plugin jacocoPlugin = codeQualityHelper.createJacocoMavenPlugin();
    Xpp3Dom configuration = MojoExecutor.configuration();
    // For Jacoco no configuration necessary for its goal "report", at least
    // not for our purposes.
    executeMojo(jacocoPlugin, "report", configuration,
            executionEnvironment(codeQualityHelper.getProject(),
                    codeQualityHelper.getMavenSession(), codeQualityHelper.getPluginManager()));

    info("Done running test phase.");
  }


  Xpp3Dom createMavenSureFireConfigurationForTest() {
    Xpp3Dom conf = MojoExecutor.configuration();
    codeQualityHelper.addSimpleTag("argLine", "${jacocoArgLine} -Xmx256m", conf);
    return conf;
  }




  @Override
  String mojoName() {
    return "code-quality-test";
  }
}
