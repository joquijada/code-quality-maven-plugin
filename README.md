# `code-quality-maven-plugin`
> This project is a Maven extension plugin that can be used in you project to incorporate, declaratively (I.e. via Maven plugin configurations) other plugins that assist in ascertaining the quality of the code associated with that project.

<!-- MDTOC maxdepth:6 firsth1:1 numbering:0 flatten:0 bullets:1 updateOnSave:1 -->

      - [Introduction](#introduction)   
      - [How It Works](#how-it-works)   
      - [Configuration](#configuration)   
         - [scmUrl](#scmurl)   
         - [siteUpload](#siteupload)   
         - [sitePath](#sitepath)   
         - [siteMerge](#sitemerge)   
         - [packageNames](#packagenames)   

<!-- /MDTOC -->


### Introduction
Inspiration for this project came from the [Maven Code Quality Pom](https://github.com/gregswindle/maven-code-quality-pom).

This plugin conveniently wraps all of the quality reporting modules/plugins in [Maven Code Quality Pom](https://github.com/gregswindle/maven-code-quality-pom), dynamically adding and configuring each of those plugin and modules to your project. In your project all you need to do is add a few lines of  `XML` in the project's `pom.xml`. You get the full power and benefits of [Maven Code Quality Pom](https://github.com/gregswindle/maven-code-quality-pom), without having to redo the work of adding manually adding and configuring each individually. Below is a sample configuration:

```xml
<build>
  ...
  <plugins>
    ...
    <plugin>
      <groupId>com.exsoinn</groupId>
      <artifactId>code-quality-maven-plugin</artifactId>
      <!-- Very important, must enable Maven extensions-->
      <extensions>true</extensions>
      <configuration>
        <scmUrl>scm:git:https://path-to-git-project</scmUrl>
        <siteUpload>true</siteUpload>
        <sitePath>path-into-which-to-deploy-the-site</sitePath>
        <siteMerge>false</siteMerge>
        <packageNames>comma-separate-list-of-class-package-names</packageNames>
      </configuration>
    </plugin>
    ...
  </plugins>
  ...
</build>
```

### How It Works
We make use of [Maven extension mechanism](https://maven.apache.org/examples/maven-3-lifecycle-extensions.html). Class `org.apache.maven.AbstractMavenLifecycleParticipant` has been extended to modify the Maven project model after its been read, but before a plan has been put together and executed.

### Configuration
This section describes the parameters you can specify the in `<configuration/>` section of the plugin. Unless otherwise noted, each of the parameters below themselves are `XML` tags, for example `<uploadSite>true</uploadSite>`
#### scmUrl
Path to Git project, for example `scm:git:https://github.com/joquijada/element-path-finder.git`. The URL must follow the [prescribed Apache Maven Project format](https://maven.apache.org/scm/scm-url-format.html)
> Required? Yes

#### siteUpload
Set the tag value to `true` (E.g. `<siteUpload>true</siteUpload>`) to enable uploading of the Maven generated site to GitHub pages. This assumes that (a) your project is hosted on GitHub, and (b) you have enabled GitHub pages for your repository. Read [GitHub pages documentation for more info](https://help.github.com/en/articles/configuring-a-publishing-source-for-github-pages)
** Currently only uploading to GitHub pages is supported. In the future might support other kinds of site upload locations. **

> Required? No


#### sitePath
Folder in the remote server where the site gets uploaded. Say the project's associated GitHub page is at `http://owner.github.io/project`, and you have configure ` <sitePath>site-project</sitePath>`, then the contents will be uploaded to `http://owner.github.io/project/site-project`

> Required? No. Note that if `siteUpload` is `false`, then this parameter gets ignored.

#### siteMerge
Set to `true` and site contents gets merged when uploaded, but if set to `false` site contents gets overwritten.

> Required> No. Note that if `siteUpload` is `false`, then this parameter gets ignored.


#### packageNames
Comma-separated list of packages to scan for code quality and report on. Example `<packageNames>com.foo.*,com.bar.*</packageNames>`

> Required? Yes



[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat-square)](http://makeapullrequest.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
