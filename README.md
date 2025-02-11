
## sponsor

:heart: This work has been sponsored by [Transtrend](https://www.transtrend.com/) :heart:


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

## supported messages

```
To CME
New Order - Single
Order Cancel Replace Request
Order Cancel Request
Order Status Request
Order Mass Status Request
Order Mass Action Request
Request for Quote
Party Details Definition Request
Party Details List Request

From CME
Business Reject
Execution Report - New Order
Execution Report - Modify
Execution Report - Cancel
Execution Report - Status
Execution Report - Trade Outright
Execution Report - Trade Spread
Execution Report - Trade Spread Leg
Execution Report - Elimination
Execution Report - Reject
Execution Report - Trade Addendum Outright
Execution Report - Trade Addendum Spread
Execution Report - Trade Addendum Spread Leg
Execution Report Pending Cancel
Execution Report Pending Replace
Order Cancel Reject
Order Cancel Replace Reject
Request for Quote Acknowledgment
Order Mass Action Report
Party Details Definition Request Acknowledgment
Party Details List Report
```
