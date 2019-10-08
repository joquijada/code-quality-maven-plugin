package com.exsoinn.maven.plugin;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.Reporting;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;


/**
 * @author josequijada
 */
@RunWith(MockitoJUnitRunner.class)
public class CodeQualityMavenLifecycleParticipantTest {

  private CodeQualityMavenLifecycleParticipant codeQualityMavenLifecycleParticipant;

  private CodeQualityHelper codeQualityHelper = mock(CodeQualityHelper.class);



  @Before
  public void setUp() {
    codeQualityMavenLifecycleParticipant = new CodeQualityMavenLifecycleParticipant(
            codeQualityHelper);
    when(codeQualityHelper.readParameter(any(MavenProject.class), eq("packageNames"))).thenReturn("blah");
  }


  /**
   * Basic sanity test of argless constructor
   */
  @Test
  public void testInstantitesCodeQUalitHelperItself() {
    CodeQualityMavenLifecycleParticipant cqmlp = new CodeQualityMavenLifecycleParticipant();
    assertTrue(null != cqmlp.getCodeQualityHelper());
  }


  @Test
  public void testInvokesCodeQualityHelper() throws MavenExecutionException {
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());

    /*
     * Ooohhh yeahh! Let the verifications begin
     */
    verify(codeQualityHelper, atLeast(1)).createJacocoPlugin(any(List.class));
    //verify(codeQualityHelper, atLeast(1)).reConfigureMavenSurefirePlugin(any(MavenProject.class));
    verify(codeQualityHelper, atLeast(1)).addReportingPlugins(any(Reporting.class));
    verify(codeQualityHelper, atLeast(1)).autoConfigureMavenSitePlugin(any(MavenProject.class));
  }


  @Test
  public void testScmInfoLogicSkipped() throws MavenExecutionException {
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());
    verify(codeQualityHelper, never()).addScmInfo(any(MavenProject.class), any(String.class));
  }

  @Test
  public void testScmInfoAddedWhenScmUrlIsPresent() throws MavenExecutionException {
    MavenSession mavenSession = mockMavenSession();
    final String fakeUrl = "http://fake.url.com/";
    when(codeQualityHelper.readParameter(any(MavenProject.class), eq("scmUrl"))).thenReturn(fakeUrl);
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mavenSession);
    verify(codeQualityHelper, atLeast(1)).addScmInfo(any(MavenProject.class), eq(fakeUrl));
  }



  @Test
  public void testThrowsExceptionWhenPackageNamesIsMissing() throws MavenExecutionException {
    boolean thrown = false;
    when(codeQualityHelper.readParameter(any(MavenProject.class), eq("packageNames"))).thenReturn(null);
    try {
      codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());
    } catch (IllegalArgumentException e) {
      thrown = true;
      assertTrue(e.getMessage().matches("Parameter 'packageNames' is required.*"));
    }
    assertTrue(thrown);
  }

  @Test
  public void siteSetupSkippedWhenNotConfigured() throws MavenExecutionException {
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());
    verify(codeQualityHelper, never()).createSiteUploadPlugin(any(String.class), any(Boolean.class));
  }

  @Test
  public void siteSetupSkippedWhenUrlNotGitHub() throws MavenExecutionException {
    MavenSession ms = mockMavenSession();
    ms.getCurrentProject().getModel().setUrl("https://gitlab.com/some-project/some-repo");
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());
    verify(codeQualityHelper, never()).createSiteUploadPlugin(any(String.class), any(Boolean.class));
  }


  /**
   * Currently only GitHub URL's supported for site upload. When/if other vendors supported in future,
   * then this unit test will need to be adjusted to reflect that. See {@link CodeQualityMavenLifecycleParticipantTest#mockMavenSession()},
   * and search for "setUrl()" to see where we set the project's URL.
   * @throws MavenExecutionException
   */
  @Test
  public void siteConfiguredWhenSiteUploadPresentAndUrlContainsGitHub() throws MavenExecutionException {
    when(codeQualityHelper.readParameter(any(MavenProject.class), eq("siteUpload"))).thenReturn("true");
    codeQualityMavenLifecycleParticipant.afterProjectsRead(mockMavenSession());
    verify(codeQualityHelper, atLeast(1)).createSiteUploadPlugin(any(String.class), any(Boolean.class));
  }


  private MavenSession mockMavenSession() {
    MavenExecutionRequest req = mock(MavenExecutionRequest.class);
    MavenSession ms = new MavenSession(null, null, req,
            null);
    MavenProject mp = new MavenProject();
    ms.setCurrentProject(mp);
    Model model = new Model();
    model.setUrl("https://github.com/some-project/some-repo");
    mp.setModel(model);
    Build build = new Build();
    Plugin plugin = new Plugin();
    plugin.setGroupId("com.exsoinn");
    plugin.setArtifactId(CodeQualityHelper.PLUGIN_ARTIFACTID);
    plugin.setGroupId(CodeQualityHelper.PLUGIN_GROUP);
    build.setPlugins(new ArrayList<>(Collections.singletonList(plugin)));
    Xpp3Dom config = new Xpp3Dom("configuration");
    Xpp3Dom pkgNames = new Xpp3Dom("packageNames");
    config.addChild(pkgNames);
    pkgNames.setValue("foo");
    plugin.setConfiguration(config);
    mp.setBuild(build);
    // Plugin plugin = pProject.getBuild().getPluginsAsMap().get(PLUGIN_NAME);
    return ms;
  }


  private void addParameter(MavenSession pSess, String pName, String pVal) {
    Plugin p = pSess.getCurrentProject().getBuild().getPluginsAsMap().get(CodeQualityHelper.PLUGIN_KEY);
    Xpp3Dom config = (Xpp3Dom) p.getConfiguration();
    Xpp3Dom child = new Xpp3Dom(pName);
    child.setValue(pVal);
    config.addChild(child);
  }


  private void removeParameter(MavenSession pSess, String pName) {
    Plugin p = pSess.getCurrentProject().getBuild().getPluginsAsMap().get(CodeQualityHelper.PLUGIN_KEY);
    Xpp3Dom config = (Xpp3Dom) p.getConfiguration();
    int toRemove = -1;
    for (Xpp3Dom c : config.getChildren()) {
      if (toRemove < 0) {
        toRemove = 0;
      } else {
        ++toRemove;
      }
      if (pName.equals(c.getName())) {
        break;
      }
    }
    config.removeChild(toRemove);
  }

}