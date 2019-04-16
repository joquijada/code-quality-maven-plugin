package com.exsoinn.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;


/**
 *
 */
public abstract class AbstractCodeQualityMojo extends AbstractMojo {

  protected final CodeQualityHelper codeQualityHelper;

  public AbstractCodeQualityMojo() {
    codeQualityHelper = new CodeQualityHelper();
  }

  /**
   *
   * @return - The name of this Mojo, child classes responsible for implementing
   */
  abstract String mojoName();

  String addMojoName(String pMsg) {
    return "[" + mojoName() + "]: " + pMsg;
  }

  /**
   * Format the info message according to our wants.
   * @param pMsg
   */
  void info(String pMsg) {
    getLog().info(addMojoName(pMsg));
  }

}
