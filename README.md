# JUnit 5 Support for Eclipse

This is a fork of the Eclipse JDT project with experimental support for (JUnit 5)[https://github.com/junit-team/junit5], currently based on JUnit 5.0.0-ALPHA.

This project supports a command line build with Maven and Tycho, and an interactive build in Eclipse, using a largely automatic setup with Oomph.

## Prerequisites

* Java 8
* Maven 3.3.9
* Eclipse Installer (Oomph) 1.4.0

## Command line build

````
git clone https://github.com/hwellmann/org.junit.gen5
cd org.junit.gen5
mvn install

git clone https://github.com/hwellmann/eclipse.jdt.ui
cd eclipse.jdt.ui
mvn install -DskipTests -Pbuild-individual-bundles
````

## Setup for interactive build

* Clone the `org.junit.gen5` repository, see above. You'll need the Oomph project setup from `setup/JDTWithJUnit5.setup`.
* Launch Eclipse Installer.
* If the Installer opens in simple mode (with a heading of _eclipse installer by Oomph_), open the menu (hamburger icon in top right corner) and switch to advanced mode).
* Select Eclipse IDE for Eclipse Committers.
* Select Product Version Mars
* Select a Java 8 VM.
* Click Next. The Project panel opens. 
* Click on the Plus icon to add a user project.
* Select Catalog Github Projects
* Click on *Browse File System* and select `JDTWithJUnit5.setup` from your local clone of the `org.junit.gen5` repository.
* Click OK. In the Projects tree you should now see an entry *JDT with JUnit 5* under *Github Projects | <User>*.
* Open this entry and select the subitems *UI* and *UI | Tests*
* Click *Next*. The *Variables* panel opens.
* Click on *Show all variables*.
* For the two Github repositories, open the dropdown and make sure that *HTTPS (anonymous access) is selected.
* Click *Next*. The *Confirmation* panel opens.
* Click *Finish*. Oomph will start downloading Eclipse components.
* After a while, a dialog appears *Do you trust these certificates?* Active the checkbox and click *OK*.
* Click *Finish* to close the installer and wait for your new Eclipse installation to open.
* Close the *Welcome* view.
* Click on the rotating arrows at the bottom to monitor further setup tasks.
* Cloning the forked JDT repository will take a while.
* Wait for the setup to complete and click *Finish* to close the setup dialog.
* Your workspace now contains a project `org.junit.gen5` with an error marker.
* Open a shell, go to the root directory of that project and run `mvn install`.
* Refresh the project (F5). All error markers should disappear.

## Run Eclipse with JUnit 5 support

* Select the `org.junit.gen5` project and execute *Run As | Eclipse Application* from the context menu.
* Wait for Eclipse Neon to open.
* Switch to the Java perspective.
* Import the `junit5-demo` project from `org.junit.gen5/projects` via *File | Import... | General | Existing Projects into Workspace*.
* Select a class or package from that project and execute *Run As | JUnit Test* from the context menu.
_____

Contributing to JDT UI - Java development tools UI
============================================

Thanks for your interest in this project.

Project description:
--------------------

The JDT UI implements the user interface for the Java IDE. This includes views like Package Explorer and JUnit, the Java and properties files editors, Java search, and refactorings.
Website: <http://www.eclipse.org/jdt/ui/>

- <https://projects.eclipse.org/projects/eclipse.jdt.ui>

How to contribute:
--------------------
Contributions to JDT UI are most welcome. There are many ways to contribute, 
from entering high quality bug reports, to contributing code or documentation changes. 
For a complete guide, see the [How to Contribute] [1] page on the team wiki.

Developer resources:
--------------------

Information regarding source code management, builds, coding standards, and more.

- <https://projects.eclipse.org/projects/eclipse.jdt.ui/developer>

Contributor License Agreement:
------------------------------

Before your contribution can be accepted by the project, you need to create and electronically sign the Eclipse Foundation Contributor License Agreement (CLA).

- <http://www.eclipse.org/legal/CLA.php>

Forum:
------

Public forum for Eclipse JDT users.

- <http://www.eclipse.org/forums/eclipse.tools.jdt>

Search for bugs:
----------------

This project uses Bugzilla to track ongoing development and issues.

- <https://bugs.eclipse.org/bugs/buglist.cgi?product=JDT;component=UI>

Create a new bug:
-----------------

Be sure to search for existing bugs before you create another one. Remember that contributions are always welcome!

- <https://bugs.eclipse.org/bugs/enter_bug.cgi?product=JDT;component=UI>

Contact:
--------

Contact the project developers via the project's "dev" list.

- <https://dev.eclipse.org/mailman/listinfo/jdt-ui-dev>

License
-------

[Eclipse Public License (EPL) v1.0][2]

[1]: https://wiki.eclipse.org/JDT_UI/How_to_Contribute
[2]: http://wiki.eclipse.org/EPL
