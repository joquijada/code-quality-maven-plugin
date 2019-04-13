package com.exsoinn.maven.plugin;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.codehaus.plexus.component.annotations.Component;


/**
 * @author josequijada
 */
@Component(role = AbstractMavenLifecycleParticipant.class)
public class CodeQualityMavenLifecycleParticipant extends AbstractMavenLifecycleParticipant {

  private final CodeQualityHelper codeQualityHelper;

  public CodeQualityMavenLifecycleParticipant() {
    codeQualityHelper = new CodeQualityHelper();
  }


  @Override
  public void afterProjectsRead(MavenSession pSession)
          throws MavenExecutionException {
    // Add Jacoco plugin
    pSession.getCurrentProject().getModel().getBuild().getPlugins()
            .add(codeQualityHelper.createJacocoPlugin());
    // Reconfigure Maven surefire plugin
    codeQualityHelper.reConfigureMavenSurefirePlugin(pSession.getCurrentProject());

    // Add the various reporting plugins
    codeQualityHelper.addReportingPlugins(pSession.getCurrentProject().getModel().getReporting());
  }
}
