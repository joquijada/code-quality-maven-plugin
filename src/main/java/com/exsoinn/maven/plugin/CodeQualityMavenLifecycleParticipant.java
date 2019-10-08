package com.exsoinn.maven.plugin;

import java.util.Arrays;
import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.StringUtils;


/**
 * @author josequijada
 */
@Component(role = AbstractMavenLifecycleParticipant.class, hint = "codeQuality")
public class CodeQualityMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {


  private final CodeQualityHelper codeQualityHelper;

  public CodeQualityMavenLifecycleParticipant() {
    this(new CodeQualityHelper());
  }


  public CodeQualityMavenLifecycleParticipant(CodeQualityHelper pCodeQualityHelper) {
    codeQualityHelper = pCodeQualityHelper;
  }


  @Override
  public void afterProjectsRead(MavenSession pSession)
          throws MavenExecutionException {
    codeQualityHelper.LOGGER.info("Project read, Dynamically reconfiguring the Maven model..."
            + pSession.getCurrentProject().getModel());
    // Add Jacoco plugin
    String pkgNamesAsStr = codeQualityHelper
            .readParameter(pSession.getCurrentProject(), "packageNames");
    if (StringUtils.isBlank(pkgNamesAsStr)) {
      throw new IllegalArgumentException(
              "Parameter 'packageNames' is required. Provide comma-separated list"
                      + " of Java package names to scan");
    }

    pSession.getCurrentProject().getModel().getBuild().getPlugins()
            .add(codeQualityHelper.createJacocoPlugin(Arrays.asList(pkgNamesAsStr.split(","))));
    // Reconfigure Maven surefire plugin
    //codeQualityHelper.reConfigureMavenSurefirePlugin(pSession.getCurrentProject());

    // Add the various reporting plugins
    codeQualityHelper.addReportingPlugins(pSession.getCurrentProject().getModel().getReporting());

    // Configure the Site plugin
    codeQualityHelper.autoConfigureMavenSitePlugin(pSession.getCurrentProject());

    String scmUrl = codeQualityHelper.readParameter(pSession.getCurrentProject(), "scmUrl");
    if (StringUtils.isNotBlank(scmUrl)) {
      codeQualityHelper.LOGGER.info("Configuring SCM info using URL {}", scmUrl);
      codeQualityHelper.addScmInfo(pSession.getCurrentProject(), scmUrl);
    }

    /*
     * If the project is hosted by GitHub, and if caller opted in to upload site,
     * set up the plugin for that
     */
    String projUrl = pSession.getCurrentProject().getModel().getUrl();
    Boolean uploadSite = Boolean
            .valueOf(codeQualityHelper.readParameter(pSession.getCurrentProject(), "siteUpload"));

    if (uploadSite && StringUtils.isNotBlank(projUrl) && projUrl.contains("github.com")) {
      String sitePath = codeQualityHelper.readParameter(pSession.getCurrentProject(), "sitePath");
      Boolean siteMerge = Boolean
              .valueOf(codeQualityHelper.readParameter(pSession.getCurrentProject(), "siteMerge"));
      pSession.getCurrentProject().getModel().getBuild().getPlugins()
              .add(codeQualityHelper.createSiteUploadPlugin(sitePath, siteMerge));
    }

    codeQualityHelper.LOGGER.info("Done dynamically reconfiguring the Maven model, final model is: "
            + pSession.getCurrentProject().getModel());
  }


  /*
   * Getters/Setters
   */
  public CodeQualityHelper getCodeQualityHelper() {
    return codeQualityHelper;
  }

}
