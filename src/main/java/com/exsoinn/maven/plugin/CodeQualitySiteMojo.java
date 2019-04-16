package com.exsoinn.maven.plugin;


import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.util.ArrayList;
import java.util.List;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.Xpp3Dom;

@Mojo(name = "code-quality-site", defaultPhase = LifecyclePhase.PRE_SITE)
public class CodeQualitySiteMojo extends AbstractCodeQualityMojo {



  @Override
  String mojoName() {
    return "code-quality-site";
  }

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    codeQualityHelper.addReportingPlugins(codeQualityHelper.getProject().getModel().getReporting());

    Plugin mavenSitePlugin = codeQualityHelper.autoConfigureMavenSitePlugin();
    //executeMojo(mavenSitePlugin, "pre-site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
    //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
    executeMojo(mavenSitePlugin, "site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
            executionEnvironment(codeQualityHelper.getProject(),
                    codeQualityHelper.getMavenSession(),
                    codeQualityHelper.getPluginManager()));
    //executeMojo(mavenSitePlugin, "post-site", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
    //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));

    // Get rid of config, not needed for "deploy" goal
    //mavenSitePlugin.setConfiguration(MojoExecutor.configuration());
    //executeMojo(mavenSitePlugin, "deploy", (Xpp3Dom) mavenSitePlugin.getConfiguration(),
    //executionEnvironment(getProject(), getMavenSession(), getPluginManager()));
  }


}
