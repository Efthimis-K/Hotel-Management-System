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

# General

- When making changes, always check the current codebase to understand the current state of the project and provide relevant outputs.
- If you are unsure about the current state of the project, ask for clarification.
- When you make changes, ALWAYS verify the intended changes against the current code to ensure that the changes are consistent with the current state of the project.

# Code Help

- Guideline: For code generation and library questions, use Context7: provide the relevant project files, the exact Java version (Java 21), and any dependency versions. Include the minimal reproducible code and describe the desired change or question. Context7 should return code snippets, dependency recommendations, and migration steps tailored to the current codebase and compatible with Java 21.
