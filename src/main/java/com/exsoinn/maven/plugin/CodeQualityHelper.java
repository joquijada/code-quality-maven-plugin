package com.exsoinn.maven.plugin;

import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DistributionManagement;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.ReportPlugin;
import org.apache.maven.model.ReportSet;
import org.apache.maven.model.Reporting;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twdata.maven.mojoexecutor.MojoExecutor;

/**
 * @author josequijada
 */
class CodeQualityHelper {

  private static final List<String> reports;

  private static final String KEY_COUNTER = "counter";
  private static final String KEY_VALUE = "value";
  private static final String KEY_MINIMUM = "minimum";

  static {
    reports = new ArrayList<>();
    reports.add("dependencies");
    reports.add("plugins");
    reports.add("license");
    reports.add("index");
    reports.add("dependency-convergence");
    reports.add("cim");
    reports.add("dependency-info");
    reports.add("dependency-management");
    reports.add("distribution-management");
    reports.add("help");
    reports.add("issue-tracking");
    reports.add("mailing-list");
    reports.add("modules");
    reports.add("plugin-management");
    reports.add("plugins");
    reports.add("project-team");
    reports.add("scm");
    reports.add("summary");
  }

  static final String MAVEN_SITE_PLUGIN = "maven-site-plugin";
  static final String MAVEN_SUREFIRE_PLUGIN = "maven-surefire-plugin";
  private static final String MAVEN_GROUP = "org.apache.maven.plugins:";
  private static final String MAVEN_SITE_PLUGIN_KEY = MAVEN_GROUP + MAVEN_SITE_PLUGIN;
  static final String MAVEN_SUREFIRE_PLUGIN_KEY = MAVEN_GROUP + MAVEN_SUREFIRE_PLUGIN;
  static final String JACOCO_ARG_LINE_PROP_NAME = "jacocoArgLine";

  static Logger LOGGER = LoggerFactory.getLogger(CodeQualityHelper.class);

  /**
   * This is required. It gives the location of the classes against which code quality check will
   * run.
   */
  @Parameter(required = true)
  private String packageName;

  /**
   * The current project that Maven is building
   */
  @Parameter(defaultValue = "${project}")
  private MavenProject project;

  /**
   * The current Maven session
   */
  @Parameter(defaultValue = "${session}")
  private MavenSession mavenSession;


  @Component
  private BuildPluginManager pluginManager;


  DistributionManagement buildDistributionManagement(MavenProject pProj) {
    DistributionManagement dm = new DistributionManagement();
    pProj.setDistributionManagement(dm);
    return null;
  }


  Plugin createJacocoMavenPlugin() {
    return createMavenPlugin("org.jacoco", "jacoco-maven-plugin", "0.7.9");
  }


  Plugin createJacocoReportMavenPlugin() {
    return createMavenPlugin("org.jacoco", "jacoco-maven-plugin", "0.7.9");
  }


  private Plugin createMavenPlugin(String pGroupId, String pArtifactId, String pVersion) {
    return MojoExecutor.plugin(pGroupId, pArtifactId, pVersion);
  }


  private ReportPlugin createMavenReportPlugin(String pGroupId, String pArtifactId,
          String pVersion) {
    ReportPlugin rp = new ReportPlugin();
    rp.setGroupId(pGroupId);
    rp.setArtifactId(pArtifactId);
    rp.setVersion(pVersion);
    return rp;
  }


  Plugin createJacocoPlugin() {
    final String prepAgent = "prepare-agent";
    Plugin p = createJacocoMavenPlugin();
    // 1. Add Jacoco init execution, the one that sets up the jacocoArgLine via the
    // "prepare-agent" goal
    PluginExecution execPrepareAgent = new PluginExecution();
    p.addExecution(execPrepareAgent);
    execPrepareAgent.setId(prepAgent);
    execPrepareAgent.setGoals(Collections.singletonList(prepAgent));
    execPrepareAgent.setConfiguration(configureJacocoMavenPluginForInitialize());

    // 2. Set up Jacoco "check" goal execution
    final String check = "check";
    PluginExecution execCheck = new PluginExecution();
    execCheck.setId(check);
    execPrepareAgent.setGoals(Collections.singletonList(check));
    p.addExecution(execCheck);
    execCheck.setConfiguration(configureJacocoMavenPluginForVerify());

    // 3. Add Jacoco's report execution
    final String report = "report";
    PluginExecution execReport = new PluginExecution();
    execReport.setId(report);
    execCheck.setPhase("test");
    execPrepareAgent.setGoals(Collections.singletonList(report));
    return p;
  }


  Xpp3Dom configureJacocoMavenPluginForVerify() {
    Xpp3Dom config = MojoExecutor.configuration();
    Map<String, List<Map<String, String>>> ruleToLimitMap = new HashMap<>();
    List<Map<String, String>> bundleList = new ArrayList<>();

    // Add a BUNDLE rule and its limits
    bundleList.add(buildLimitParamsMap("COMPLEXITY", "COVEREDRATIO", "0.20"));
    ruleToLimitMap.put("BUNDLE", bundleList);

    // Add a CLASS rule and its accompanying limits
    List<Map<String, String>> classList = new ArrayList<>();
    classList.add(buildLimitParamsMap("COMPLEXITY", "COVEREDRATIO", "0.20"));
    classList.add(buildLimitParamsMap("BRANCH", "COVEREDRATIO", "0.90"));
    classList.add(buildLimitParamsMap("LINE", "COVEREDRATIO", "0.90"));
    classList.add(buildLimitParamsMap("METHOD", "COVEREDRATIO", "0.90"));
    ruleToLimitMap.put("CLASS", classList);

    Xpp3Dom rulesParentNode = new Xpp3Dom("rules");
    config.addChild(rulesParentNode);
    for (Map.Entry<String, List<Map<String, String>>> ent : ruleToLimitMap.entrySet()) {
      addRuleNode(ent, rulesParentNode);
    }
    return config;
  }


  private Map<String, String> buildLimitParamsMap(String pCnt, String pVal, String pMin) {
    Map<String, String> map = new HashMap<>();
    map.put(KEY_COUNTER, pCnt);
    map.put(KEY_VALUE, pVal);
    map.put(KEY_MINIMUM, pMin);
    return map;
  }

  /**
   * Responsible for adding a single rule node to the passed in <rules/> node. It also builds
   * <limits/> node and adds to the created rule node. The structure built here looks something
   * like:
   *
   * <rule>
   * <element/>
   * <limits>
   * <limit>
   * <counter/> <value/> <minimum/>
   * </limit>
   * </limits>
   * </rule>
   *
   * @param pEntry - Contains the info to generate the <rule/> node
   * @param pRulesParentNode - The <rules/> parent node to which the newly create <rule/> node will
   * be added.
   */
  private void addRuleNode(Map.Entry<String, List<Map<String, String>>> pEntry,
          Xpp3Dom pRulesParentNode) {
    Xpp3Dom ruleNode = new Xpp3Dom("rule");
    pRulesParentNode.addChild(ruleNode);
    Xpp3Dom elemTag = new Xpp3Dom("element");
    elemTag.setValue(pEntry.getKey());
    ruleNode.addChild(elemTag);
    Xpp3Dom limitsParentNode = new Xpp3Dom("limits");
    ruleNode.addChild(limitsParentNode);
    for (Map<String, String> lim : pEntry.getValue()) {
      for (Map.Entry<String, String> ent : lim.entrySet()) {
        Xpp3Dom limitNode = new Xpp3Dom("limit");
        limitsParentNode.addChild(limitNode);
        Xpp3Dom tag = new Xpp3Dom(ent.getKey());
        tag.setValue(ent.getValue());
        limitNode.addChild(tag);
      }
    }

  }


  /**
   *
   */
  void addSimpleTag(String pName, String pVal, Xpp3Dom pTarget) {
    Xpp3Dom tag = new Xpp3Dom(pName);
    tag.setValue(pVal);
    pTarget.addChild(tag);
  }

  Xpp3Dom configureJacocoMavenPluginForInitialize() {
    Xpp3Dom jacocoConfig = MojoExecutor.configuration();
    Xpp3Dom propNameTag = new Xpp3Dom("propertyName");
    propNameTag.setValue(JACOCO_ARG_LINE_PROP_NAME);
    jacocoConfig.addChild(propNameTag);
    Xpp3Dom includesParentNode = new Xpp3Dom("includes");
    Xpp3Dom includesChildNode = new Xpp3Dom("include");
    includesChildNode.setValue(packageName);
    includesParentNode.addChild(includesChildNode);
    jacocoConfig.addChild(includesParentNode);
    return jacocoConfig;
  }

  boolean pluginExists(String pKey) {
    return project.getModel().getBuild().getPluginsAsMap().containsKey(pKey);
  }


  private void addDependency(String pGroupId, String pArtifactId, String pVersion,
          Plugin pTargetPlugin) {
    Dependency d = new Dependency();
    d.setGroupId(pGroupId);
    d.setArtifactId(pArtifactId);
    d.setVersion(pVersion);
    List<Dependency> dependencies = new ArrayList<>();
    dependencies.addAll(pTargetPlugin.getDependencies());
    dependencies.add(d);
    pTargetPlugin.setDependencies(dependencies);
  }


  Plugin autoConfigureMavenSitePlugin() {
    return autoConfigureMavenSitePlugin(project);
  }

  /**
   * Auto-configures the {@link CodeQualityHelper#MAVEN_SITE_PLUGIN} to suit our needs, creating it
   * first if not already present, else dependencies are cleared and added from scratch. Why are we
   * doing this here? Because we need this done before "site" phase. This Mojo is tied to
   * "initialize" phase. Later we may refactor things better if there's room for improvement.
   */
  Plugin autoConfigureMavenSitePlugin(MavenProject pProject) {
    final String mspVersion = "3.7.1";
    LOGGER.info("Re-configuring " + MAVEN_SITE_PLUGIN + "...");
    Plugin p;
    if (null == (p = pProject.getBuild().getPluginsAsMap().get(MAVEN_SITE_PLUGIN_KEY))) {
      p = createMavenPlugin("org.apache.maven.plugins", MAVEN_SITE_PLUGIN, mspVersion);
      pProject.getModel().getBuild().getPluginsAsMap().put(MAVEN_SITE_PLUGIN_KEY, p);
    } else {
      // If the Maven site plugin was already there in the model, override the version
      // with ours. If we use the default version of 3.3, then get error described here:
      // https://www.mkyong.com/maven/mvn-site-java-lang-classnotfoundexception-org-apache-maven-doxia-siterenderer-documentcontent/
      p.setVersion(mspVersion);
    }

    p.getDependencies().clear();
    addDependency("org.apache.maven.skins", "maven-fluido-skin", "1.6", p);
    addDependency("org.apache.maven.doxia", "doxia-module-markdown", "1.7", p);
    Xpp3Dom conf = MojoExecutor.configuration();
    p.setConfiguration(conf);
    addSimpleTag("inputEncoding", "UTF-8", conf);
    addSimpleTag("outputEncoding", "UTF-8", conf);

    Map<String, Artifact> pluginArtifactMap = pProject.getPluginArtifactMap();
    Artifact msp = pluginArtifactMap.get(MAVEN_SITE_PLUGIN_KEY);
    msp.setVersion(mspVersion);
    LOGGER.info("Done re-configuring " + MAVEN_SITE_PLUGIN + ".");
    return p;
  }


  /**
   * Dynamically add reporting plugins. We do this reasonably early enough so that they're present
   * during "mvn site" life-cycle later on. The assumption is that this Mojo will be invoked in a
   * phase earlier then the site phase, otherwise all bets are off. TODO: For quick test purposes do
   * here, might need to move to its own class.
   */
  void addReportingPlugins(Reporting pReportingNode) {
    LOGGER.info("Adding plugins to reporting section...");
    ReportPlugin reportsPlugin = createMavenReportPlugin("org.apache.maven.plugins",
            "maven-project-info-reports-plugin", "2.9");
    pReportingNode.addPlugin(reportsPlugin);
    ReportSet rptSet = new ReportSet();
    reportsPlugin.addReportSet(rptSet);
    for (String r : reports) {
      rptSet.addReport(r);
    }

    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-surefire-report-plugin", "2.19.1"));
    pReportingNode.addPlugin(
            createMavenReportPlugin("org.jacoco", "jacoco-maven-plugin", "0.7.9"));
    pReportingNode.addPlugin(
            createMavenReportPlugin("org.codehaus.mojo", "codenarc-maven-plugin",
                    "0.22-1"));
    pReportingNode.addPlugin(
            createMavenReportPlugin("org.codehaus.mojo", "findbugs-maven-plugin", "3.0.4"));

    /*
     *
     */
    ReportPlugin checkStylePlugin = createMavenReportPlugin("org.apache.maven.plugins",
            "maven-checkstyle-plugin", "2.17");
    pReportingNode.addPlugin(checkStylePlugin);
    Xpp3Dom checkstyleConfig = MojoExecutor.configuration();
    checkStylePlugin.setConfiguration(checkstyleConfig);
    addSimpleTag("configLocation", "google_checks.xml", checkstyleConfig);

    pReportingNode
            .addPlugin(createMavenReportPlugin("org.codehaus.mojo", "jdepend-maven-plugin", "2.0"));
    pReportingNode
            .addPlugin(createMavenReportPlugin("org.owasp", "dependency-check-maven", "3.0.2"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-dependency-plugin", "2.10"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-plugin-plugin", "3.5"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-jxr-plugin", "2.5"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-javadoc-plugin", "3.0.0-M1"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-changelog-plugin", "2.3"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.codehaus.plexus",
            "plexus-component-metadata", "1.7.1"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.basepom.maven",
            "duplicate-finder-maven-plugin", "1.2.1"));
    pReportingNode.addPlugin(createMavenReportPlugin("org.apache.maven.plugins",
            "maven-enforcer-plugin", "3.0.0-M1"));
    pReportingNode
            .addPlugin(
                    createMavenReportPlugin("org.codehaus.mojo", "license-maven-plugin", "1.14"));

    ReportPlugin pmdPlugin = createMavenReportPlugin("org.apache.maven.plugins",
            "maven-pmd-plugin", "3.8");
    pReportingNode.addPlugin(pmdPlugin);
    Xpp3Dom pmdConfig = MojoExecutor.configuration();
    pmdPlugin.setConfiguration(pmdConfig);
    addSimpleTag("linkXref", "true", pmdConfig);
    addSimpleTag("sourceEncoding", "utf-8", pmdConfig);
    addSimpleTag("minimumTokens", "100", pmdConfig);
    addSimpleTag("targetJdk", "1.8", pmdConfig);
    Xpp3Dom excludesParentNode = new Xpp3Dom("excludes");
    pmdConfig.addChild(excludesParentNode);
    addSimpleTag("exclude", "**/*Test.java", excludesParentNode);
    addSimpleTag("exclude", "**/generated/*.java", excludesParentNode);
    Xpp3Dom excludeRootsNode = new Xpp3Dom("excludeRoots");
    pmdConfig.addChild(excludeRootsNode);
    addSimpleTag("excludeRoot", "target/generated-sources/stubs", excludeRootsNode);
    addSimpleTag("skipEmptyReport", "false", pmdConfig);

    ReportPlugin versionsMavenPlugin = createMavenReportPlugin("org.codehaus.mojo",
            "versions-maven-plugin", "2.5");
    pReportingNode.addPlugin(versionsMavenPlugin);
    ReportSet vmpRptSet = new ReportSet();
    vmpRptSet.addReport("dependency-updates-report");
    vmpRptSet.addReport("plugin-updates-report");
    vmpRptSet.addReport("property-updates-report");
    versionsMavenPlugin.addReportSet(vmpRptSet);
    LOGGER.info("Done adding plugins to reporting section.");
  }


  void reConfigureMavenSurefirePlugin(MavenProject pProject) {
    LOGGER.info("Re-configuring " + MAVEN_SUREFIRE_PLUGIN + "...");
    Plugin p;
    //if (null == (p = pProject.getBuild().getPluginsAsMap().get(MAVEN_SUREFIRE_PLUGIN_KEY))) {
    p = createMavenPlugin("org.apache.maven.plugins", MAVEN_SUREFIRE_PLUGIN, "2.19.1");
    pProject.getModel().getBuild().getPluginsAsMap().put(MAVEN_SUREFIRE_PLUGIN_KEY, p);
    //}
    p.getDependencies().clear();
    addDependency("org.junit.platform", "junit-platform-surefire-provider", "1.0.1", p);
    addDependency("org.junit.jupiter", "junit-jupiter-engine", "5.0.1", p);
    Xpp3Dom conf = MojoExecutor.configuration();
    p.setConfiguration(conf);
    addSimpleTag("argLine", "${jacocoArgLine} -Xmx256m", conf);

    PluginExecution pe = new PluginExecution();
    pe.setId("default-test");
    List<PluginExecution> peList = new ArrayList<>();
    peList.add(pe);
    p.setExecutions(peList);
    LOGGER.info("Done re-configuring " + MAVEN_SITE_PLUGIN + ".");
  }


  /*
   * Getters
   */
  MavenProject getProject() {
    return project;
  }

  MavenSession getMavenSession() {
    return mavenSession;
  }

  BuildPluginManager getPluginManager() {
    return pluginManager;
  }

  String getPackageName() {
    return packageName;
  }

}
