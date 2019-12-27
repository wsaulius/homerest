#!/bin/bash
mvn clean flyway:clean $1 $2 
mvn antrun:run flyway:migrate flyway:info $1 $2 
mvn kotlin:compile kotlin:test-compile compile exec:java -Dexec.args="noargs" -Dexec.classpathScope=runtime -DsourceRoot=./src/ $1 $2 $3 | tee output.log

# mnv package  
# java -jar /home/adminas/workspace/homerest/target/concept-0.0.1-SNAPSHOT-jar-with-dependencies.jar | tee output.log 

