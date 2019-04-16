package com.exsoinn.maven.plugin;

import static com.exsoinn.maven.plugin.CodeQualityHelper.JACOCO_ARG_LINE_PROP_NAME;
import static com.exsoinn.maven.plugin.CodeQualityHelper.MAVEN_SUREFIRE_PLUGIN;
import static com.exsoinn.maven.plugin.CodeQualityHelper.MAVEN_SUREFIRE_PLUGIN_KEY;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;


/**
 * Mojo that encapsulates all logic associated with the init steps to effectuate the code quality
 * check.
 *
 * TODO: See if some of this can be moved to abstract class, because there will be more Mojo classes
 * to handle TODO:  other goals TODO: Once abstract class is created, make final package private
 * constansts for things like the plugin names TODO: Externalize certain things, like artifact
 * version. Think about it, if there's a bug fix in one of TODO:  the Maven projects we depend on,
 * do we want to be updating the Java for that? For now hardcoded.
 */
@Mojo(name = "code-quality-initialize", defaultPhase = LifecyclePhase.INITIALIZE)
public class CodeQualityInitializeMojo extends AbstractCodeQualityMojo {


  /**
   *
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    //info("Jacoco will be run against this package: " + codeQualityHelper.getPackageName());
    Plugin jacocoPlugin = codeQualityHelper.createJacocoMavenPlugin();

    /**
     * We could introduce validate phase Mojo and re-configure Jacoco in that phase like we're doing
     * for {@link this#MAVEN_SITE_PLUGIN} (autoConfigureMavenSitePlugin(getProject()) in this Mojo/phase,
     * but since
     * we have to execute Jacoco anyways on behalf of the project that uses this plugin,
     * let's re-configure Jacoco here, right before it is needed.
     * TODO: Account for scenario where project has already configured Jacoco; need to clear configuration
     * TODO: in that case, like we're doing for {@link this#MAVEN_SITE_PLUGIN}. Also this re-config stuff
     * TODO: needs to be strongly documented in README.md.
     */
    executeMojo(jacocoPlugin, "prepare-agent",
            codeQualityHelper.configureJacocoMavenPluginForInitialize(null),
            executionEnvironment(codeQualityHelper.getProject(),
                    codeQualityHelper.getMavenSession(), codeQualityHelper.getPluginManager()));
    info("The jacoco prepare-agent goal defined the following project property: "
            + codeQualityHelper.getProject().getProperties()
            .getProperty(JACOCO_ARG_LINE_PROP_NAME));

    autoConfigureMavenSurefirePlugin(codeQualityHelper.getProject());
    info(codeQualityHelper.getProject().getModel().toString());
  }


  void autoConfigureMavenSurefirePlugin(MavenProject pProject) {
    getLog().info("Re-configuring " + MAVEN_SUREFIRE_PLUGIN + "...");
    Plugin p;
    if (null == (p = pProject.getBuild().getPluginsAsMap().get(MAVEN_SUREFIRE_PLUGIN_KEY))) {
      p = plugin("org.apache.maven.plugins", MAVEN_SUREFIRE_PLUGIN, "2.19.1");
      pProject.getModel().getBuild().getPluginsAsMap().put(MAVEN_SUREFIRE_PLUGIN_KEY, p);
    }
    p.setVersion("2.19.1");
    //p.getDependencies().clear();
    //addDependency("org.junit.platform", "junit-platform-surefire-provider", "1.0.1", p);
    //addDependency("org.junit.jupiter", "junit-jupiter-engine", "5.0.1", p);
    Xpp3Dom conf = MojoExecutor.configuration();
    p.setConfiguration(conf);
    codeQualityHelper.addSimpleTag("argLine", "${jacocoArgLine} -Xmx256m", conf);

        /*PluginExecution pe = new PluginExecution();
        pe.setId("default-test");
        List<PluginExecution> peList = new ArrayList<>();
        peList.add(pe);
        p.setExecutions(peList);*/
    getLog().info("Done re-configuring " + MAVEN_SUREFIRE_PLUGIN + ".");
  }


    /*-<plugin>

<groupId>org.apache.maven.plugins</groupId>

<artifactId>maven-surefire-plugin</artifactId>

<version>${maven.surefire.plugin.version}</version>


-<configuration>
<!-- JaCoCo (2) Setup the argLine and run the unit tests. **NOTE the "jacocArgLine" property was configured the "prepare-agent" goal of Jacoco (see below). If you want to resolve the property value as late as possible (i.e. your dynamically alter this property value before reaching this phase), Maven gives you option to use @{jacocoArgLine} instead -->
<argLine>${jacocoArgLine} -Xmx256m</argLine>

</configuration>


-<dependencies>
-<dependency>
<groupId>org.junit.platform</groupId>
<artifactId>junit-platform-surefire-provider</artifactId>
<version>1.0.1</version>
</dependency>


-<dependency>
<groupId>org.junit.jupiter</groupId>
<artifactId>junit-jupiter-engine</artifactId>
<version>5.0.1</version>

-<execution>
<id>default-test</id>
</execution>
</executions>
</plugin>*/



  @Override
  String mojoName() {
    return "code-quality-initialize";
  }
}
