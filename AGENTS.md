# Side java project.

- This is a Java project simulating a hotel management system.
- It includes a console application and a GUI application.
- ALWAYS read the current codebase to understand the current state of the project and provide relevant outputs.

# Tech stack

- Tech stack is Java 21, Maven, and the plugins and dependencies described in `pom.xml` at the project root.

# Upgrade Dependencies

- If major upgrades to versions are needed, first check the compatibility of the dependencies with the java version.
- Also, check the compatibility of the dependencies with the current codebase. If there is a major change in the dependency, then the codebase needs to be updated accordingly.

# Run the project

- To compile and package the project, use `mvn clean package`.
- To run the current console application, use `mvn exec:java`.
- To run the current GUI application, use `mvn javafx:run`.
- Do NOT use `java -jar target/hotel-1.0-SNAPSHOT.jar` unless the build is updated to produce an executable JAR with a `Main-Class` manifest entry.
- Testing is initiated with `mvn test`.
- The default terminal is PowerShell, so prioritize that.
