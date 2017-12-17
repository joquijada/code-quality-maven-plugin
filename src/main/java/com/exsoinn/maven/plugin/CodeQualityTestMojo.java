package com.exsoinn.maven.plugin;


import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

@Mojo(name = "code-quality-test", defaultPhase = LifecyclePhase.TEST)
public class CodeQualityTestMojo extends AbstractCodeQualityMojo {
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }


    @Override
    String mojoName() {
        return "code-quality-test";
    }
}
