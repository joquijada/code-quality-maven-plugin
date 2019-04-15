package com.exsoinn.maven.plugin;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.xml.Xpp3Dom;


/**
 * @author josequijada
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "codeQuality")
public class CodeQualityMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {
  private static final String PLUGIN_NAME = "com.exsoinn:code-quality-maven-plugin";

  private final CodeQualityHelper codeQualityHelper;

  public CodeQualityMavenLifecycleParticipant() {
    codeQualityHelper = new CodeQualityHelper();
  }


  @Override
  public void afterProjectsRead(MavenSession pSession)
          throws MavenExecutionException {
    codeQualityHelper.LOGGER.info("Project read, Dynamically reconfiguring the Maven model..."
            + pSession.getCurrentProject().getModel());
    // Add Jacoco plugin
    pSession.getCurrentProject().getModel().getBuild().getPlugins()
            .add(codeQualityHelper.createJacocoPlugin());
    // Reconfigure Maven surefire plugin
    codeQualityHelper.reConfigureMavenSurefirePlugin(pSession.getCurrentProject());

    // Add the various reporting plugins
    codeQualityHelper.addReportingPlugins(pSession.getCurrentProject().getModel().getReporting());

    // Configure the Site plugin
    codeQualityHelper.autoConfigureMavenSitePlugin(pSession.getCurrentProject());


    String scmUrl = readParameter(pSession.getCurrentProject(), "scmUrl");
    if (StringUtils.isNotBlank(scmUrl)) {
      codeQualityHelper.LOGGER.info("Configuring SCM info using URL {}", scmUrl);
      codeQualityHelper.addScmInfo(pSession.getCurrentProject(), scmUrl);
    }

    codeQualityHelper.LOGGER.info("Done dynamically reconfiguring the Maven model, final model is: "
            + pSession.getCurrentProject().getModel());
  }


  private String readParameter(MavenProject pProject, String pName) {
    Plugin plugin = pProject.getBuild().getPluginsAsMap().get(PLUGIN_NAME);
    Xpp3Dom config= (Xpp3Dom) plugin.getConfiguration();
    Xpp3Dom child = config.getChild(pName);
    return null != child ? child.getValue() : null;
  }
}
