package com.exsoinn.maven.plugin;

import java.util.Arrays;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
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
    String pkgNamesAsStr = readParameter(pSession.getCurrentProject(), "packageNames");
    if (StringUtils.isBlank(pkgNamesAsStr)) {
      throw new IllegalArgumentException("Parameter 'packageNames' is required. Provide comma-separated list"
              + " of Java package names to scan");
    }

    pSession.getCurrentProject().getModel().getBuild().getPlugins()
            .add(codeQualityHelper.createJacocoPlugin(Arrays.asList(pkgNamesAsStr.split(","))));
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

    /*
     * If the project is hosted by GitHub, and if caller opted in to upload site,
     * set up the plugin for that
     */
    String projUrl = pSession.getCurrentProject().getModel().getUrl();
    Boolean uploadSite = Boolean.valueOf(readParameter(pSession.getCurrentProject(), "uploadSite"));

    if (uploadSite && StringUtils.isNotBlank(projUrl) && projUrl.contains("github.com")) {
      String sitePath = readParameter(pSession.getCurrentProject(), "sitePath");
      Boolean siteMerge = Boolean.valueOf(readParameter(pSession.getCurrentProject(), "siteMerge"));
      pSession.getCurrentProject().getModel().getBuild().getPlugins()
              .add(codeQualityHelper.createSiteUploadPlugin(sitePath, siteMerge));
    }

    codeQualityHelper.LOGGER.info("Done dynamically reconfiguring the Maven model, final model is: "
            + pSession.getCurrentProject().getModel());
  }


  private String readParameter(MavenProject pProject, String pName) {
    Plugin plugin = pProject.getBuild().getPluginsAsMap().get(PLUGIN_NAME);
    Xpp3Dom config = (Xpp3Dom) plugin.getConfiguration();
    Xpp3Dom child = config.getChild(pName);
    return null != child ? child.getValue() : null;
  }
}
