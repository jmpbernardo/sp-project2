# Install the JAR into local Maven repo

```
mvn install:install-file -Dfile=metrics-lib-2.28.0.jar -DgroupId=org.torproject -DartifactId=metrics-lib -Dversion=2.28.0 -Dpackaging=jar
```