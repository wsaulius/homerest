#!/bin/bash

#mvn antrun:run $1 $2
mvn clean 
find ./target/ -print -delete 

find ./src/main/resources/db/migration/ -type f -name \*.sql -print -delete   
cp -v ./src/main/resources/profiles/V*__Create_tables-devt.sql ./src/main/resources/db/migration/
cp -v ./src/main/resources/profiles/hibernate-devt.properties ./src/main/resources/hibernate.properties
cp -v ./src/main/resources/profiles/application-devt.properties ./src/main/resources/application.properties 

mvn kotlin:compile kotlin:test-compile compile exec:java -Dexec.args="noargs" -Dexec.classpathScope=runtime -DsourceRoot=./src/ $1 $2 $3 | tee output.log

# mnv package  
# java -jar /home/adminas/workspace/homerest/target/concept-0.0.1-SNAPSHOT-jar-with-dependencies.jar | tee output.log 

