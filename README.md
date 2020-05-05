# !!! THIS PLUGIN IS DEPRECATED !!!

**The Load Impact SaaS service is deprecated and will be end-of-lifed at December 31st, 2020. It has been superceded by [k6](https://github.com/loadimpact/k6) and [k6 Cloud](https://k6.io/cloud).**

Load Impact Jenkins plugin
=========================

A plugin for the [Jenkins CI Server](https://jenkins-ci.org/), that run load-tests hosted on [Load Impact](https://loadimpact.com/), 
as a build-step or post-build-step.

Where to get it?
------------------
* At the [Load Impact Developers page](http://developers.loadimpact.com/continuous-delivery/index.html#li-docs-cd-jenkins)
* At the [Jenkins plugins list](https://wiki.jenkins-ci.org/display/JENKINS/Load+Impact+Plugin)
* By cloning this [GitHub repo](https://github.com/loadimpact/loadimpact-jenkins-plugin) and building it using the instructions below

Building the plugin
====
You can clone this GitHub repo and build the plugin yourself using the instructions below.

Prerequisites
----
* Java JDK, version 6+
* Apache Maven, version 3+

Build
----
Run the following command (in the root directory of the cloned sources) to build the plugin ZIP

    cd path/to/loadimpact-jenkins-plugin
    mvn clean package

You can then find the plugin as the file `./target/LoadImpact-Jenkins-plugin.hpi`

Install
----

Use Jenkins own plugin upload service, by navigating to "Manage Jenkins", then "Manage Plugins", finally choose the "Advanced tab".
Under "Upload Plugin", select the HPI file you just built and click "Upload". At last, restart jenkins.

Configure
----

* Get an API token from your [Load Impact account](https://loadimpact.com/account/)
* Add a credential, defining the API token. N.B. you need to have the [Jenkins Credentials plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin) installed.
* Create a new build job and choose "Load Impact" as a build or post-build step.

More Information
============

* How to configure this plugin, see [Load Impact Jenkins Plugin](http://developers.loadimpact.com/continuous-delivery/index.html#li-docs-cd-jenkins)
* Overview of all developer support, see [Load Impact Developers](http://developers.loadimpact.com/)
* How to write load-test scripts, see [Load Script API Documentation](https://loadimpact.com/load-script-api)
* How to guides, see [Load Impact Knowledge Base](http://support.loadimpact.com/)

