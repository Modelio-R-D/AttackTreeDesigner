Attack Tree Designer
====
<b> Attack Tree Designer </b> is an Open Source Modelling Tool conceived for designing [<b> Attack Trees </b>](https://en.wikipedia.org/wiki/Attack_tree). <b>Attack Tree Designer</b> is deployed as a module in [<b>Modelio</b>](https://www.modelio.org/) modelling environent.

<b> Attack Tree Designer </b> allows users to design attack trees diagrams showing how an asset or a target might be attacked. These diagrams are intended for security specialists for modelling the attacks that occur on IT systems or cyber-physical systems and describe the events that lead to the attack in the form of a tree in which the attack is represented by the root element which is related with “OR” and “AND” conditions to the children that represent the sequence of events that lead to the root attack.

This module offers an ergonomic environment for designers and contains additional set of features that allow users to configure security attributes for the attacks such as the severity and the likelihood of the attack. It allows users to attach counter-measures to their attacks and to detect uncountered attacks. Moreover it allows referencing other trees, importing and exporting attack trees. 

For more information, visit our [**Wiki**](https://github.com/cpswarm/modelio-attack-tree-module/wiki) page.  


![](http://forge.modelio.org/attachments/download/22610/Example.png)
        **Attack Tree Designer Environment**


Attack Tree Designer - User Manual
----
Find the documentation of Attack Tree Designer at the wiki page : [Attack Tree Designer - User Manual](http://forge.modelio.org/projects/attack-tree-modelio38-user-manual-english/wiki/Wiki)


Dependencies
----

* [Java 8 JDK](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
* [Modelio 3.8](https://www.modelio.org/downloads/download-modelio.html)          <img alt="Modelio" src="https://www.modelio.org/images/logo-modelio-v4.png" width="100">

Install
----
* Download our latest [Release](https://github.com/cpswarm/modelio-attack-tree-module/releases) and extract the module archive which is a JMDAC file (with *.jmdac extension).
* (Alternative) Download this repository, retrieve the JMDAC file by compiling the source code with Maven : 
```
	mvn package
```
* Download and Install [Modelio 3.8](https://www.modelio.org/downloads/download-modelio.html)  and open a new UML project.
* Deploy <b> Attack Tree Designer </b> module (JMDAC file). For more details on how to deploy the module in Modelio see [Deployment of "Attack Tree Designer" module in Modelio](https://github.com/cpswarm/modelio-attack-tree-module/wiki/Deployment-of-%22Attack-Tree-Designer%22-module-in-Modelio).


Contributing
----

Contributions are welcome. 

Please fork and make your changes. There is no need to pull request since this is only a mirror project to the SVN repository at [Modelio Community Forge](http://forge.modelio.org/projects/attacktree-modelio38). For major changes, please open an issue and discuss it with the other authors.


Affiliation
----

![CPSwarm](https://github.com/cpswarm/template/raw/master/cpswarm.png)  
This module is developed with :heart: by Softeam R&D Department and this work is supported by the European Commission through the [CPSwarm H2020 project](https://cpswarm.eu) under grant no. 731946.


