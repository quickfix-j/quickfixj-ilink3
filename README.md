
## How to build

1. Build artio iLink3 classes from here: https://github.com/quickfix-j/artio-ilink3
2. Until a release has been deployed to github packages or maven central you need to add the artio iLink3 libs to your local maven repo. So after the build, do this in your build directory:

```
# libs
mvn install:install-file    -Dfile=artio-core/build/libs/artio-core-0.154-SNAPSHOT.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-core    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=artio-codecs/build/libs/artio-codecs-0.154-SNAPSHOT.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-codecs    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=artio-ilink3-impl/build/libs/artio-ilink3-impl-0.154-SNAPSHOT.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-ilink3-impl    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true
mvn install:install-file    -Dfile=artio-ilink3-codecs/build/libs/artio-ilink3-codecs-0.154-SNAPSHOT.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-ilink3-codecs    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true

# sources
mvn install:install-file    -Dfile=artio-core/build/libs/artio-core-0.154-SNAPSHOT-sources.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-core    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true -Dclassifier=sources
mvn install:install-file    -Dfile=artio-codecs/build/libs/artio-codecs-0.154-SNAPSHOT-sources.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-codecs    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true -Dclassifier=sources
mvn install:install-file    -Dfile=artio-ilink3-impl/build/libs/artio-ilink3-impl-0.154-SNAPSHOT-sources.jar -DgroupId=uk.co.real-logic    -DartifactId=artio-ilink3-impl    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true -Dclassifier=sources
mvn install:install-file    -Dfile=artio-ilink3-codecs/build/libs/artio-ilink3-codecs-0.154-SNAPSHOT-sources.jar    -DgroupId=uk.co.real-logic    -DartifactId=artio-ilink3-codecs    -Dversion=0.154-SNAPSHOT    -Dpackaging=jar    -DgeneratePom=true -Dclassifier=sources
```

3. Build this project using

```
./mvnw clean package
```
