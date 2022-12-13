# Running the application

## Cleaning the project
Deletes the target directory
```bash
mvn clean
```

## Executing the application
Starts the application
```bash
mvn exec:java
```

## Running tests
Requires the maven-surefire-plugin plugin
```bash
mvn test
```

## Verifying the module
Builds the project, runs all the test cases and runs check on the integration tests
```bash
mvn verify
```

## Generating the dependency tree
```bash
mvn dependency:tree
```

## Compile the source Java classes
```bash
mvn compile
```

## Builds the maven and install the project files, deploying the JAR file to the local repository
```bash
mvn install
```

## Build the maven project into a JAR, converting it into a distributable format
```bash
mvn package
```