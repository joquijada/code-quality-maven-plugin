package com.exsoinn.maven.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

@Mojo(name = "code-quality-verify", defaultPhase = LifecyclePhase.VERIFY)
public class CodeQualityVerifyMojo extends AbstractCodeQualityMojo {



  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    info("Running verify stage");
    Plugin jacocoPlugin = codeQualityHelper.createJacocoMavenPlugin();
    executeMojo(jacocoPlugin, "check", codeQualityHelper.configureJacocoMavenPluginForVerify(),
            executionEnvironment(codeQualityHelper.getProject(),
                    codeQualityHelper.getMavenSession(), codeQualityHelper.getPluginManager()));

    info("Done running verify stage.");
  }





  @Override
  String mojoName() {
    return "code-quality-verify";
  }
}