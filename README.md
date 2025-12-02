
# Instructions to compile and execute the project

## Install the Tor Metrics JAR into local Maven repo

```bash 
cd src/main/resources/ 
mvn install:install-file -Dfile=metrics-lib-2.28.0.jar -DgroupId=org.torproject -DartifactId=metrics-lib -Dversion=2.28.0 -Dpackaging=jar
```

## Compile and start project

```bash
mvn clean package -DskipTests
mvn spring−boot:run
```

## Compile and execute tests

```bash
mvn clean compile package
```
