package com.exsoinn.maven.plugin;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.util.*;

import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;

@Mojo(name = "code-quality-verify", defaultPhase = LifecyclePhase.VERIFY)
public class CodeQualityVerifyMojo extends AbstractCodeQualityMojo {
    private static final String KEY_COUNTER = "counter";
    private static final String KEY_VALUE = "value";
    private static final String KEY_MINIMUM = "minimum";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        info("Running verify stage");
        Plugin jacocoPlugin = createJacocoMavenPlugin();
        Xpp3Dom configuration = MojoExecutor.configuration();
        configureJacocoMavenPluginForVerify(configuration);
        executeMojo(jacocoPlugin, "check", configuration, executionEnvironment(getProject(),
                getMavenSession(), getPluginManager()));

        info("Done running verify stage.");
    }


    private void configureJacocoMavenPluginForVerify(Xpp3Dom pConfig) {
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
        pConfig.addChild(rulesParentNode);
        for (Map.Entry<String, List<Map<String, String>>> ent : ruleToLimitMap.entrySet()) {
            addRuleNode(ent, rulesParentNode);
        }
    }


    /**
     * Responsible for adding a single rule node to the passed in <rules/> node. It also
     * builds <limits/> node and adds to the created rule node. The structure built here looks
     * something like:
     *
     * <rule>
     *     <element/>
     *     <limits>
     *         <limit>
     *             <counter/>
     *             <value/>
     *             <minimum/>
     *         </limit>
     *     </limits>
     * </rule>
     *
     * @param pEntry - Contains the info to generate the <rule/> node
     * @param pRulesParentNode - The <rules/> parent node to which the newly create <rule/> node will
     *                         be added.
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

    private Map<String, String> buildLimitParamsMap(String pCnt, String pVal, String pMin) {
        Map<String, String> map = new HashMap<>();
        map.put(KEY_COUNTER, pCnt);
        map.put(KEY_VALUE, pVal);
        map.put(KEY_MINIMUM, pMin);
        return map;
    }

    @Override
    String mojoName() {
        return "code-quality-verify";
    }
}