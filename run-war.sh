#!/bin/bash

find ./target/ -print -delete 
mvn clean kotlin:compile compile war:war 
mvn cargo:deploy -X
#java -Dlog4j.debug=true -Dlog4j.configuration=file:///home/adminas/workspace/homerest/src/main/resources/log4j.properties -cp /home/adminas/workspace/homerest/target/concept-0.0.1-SNAPSHOT-jar-with-dependencies.jar org.eclipse.jetty.quickstart.PreconfigureQuickStartWar  /home/adminas/workspace/homerest/target/concept-0.0.1-SNAPSHOT.war

# mnv package  
# java -jar /home/adminas/workspace/homerest/target/concept-0.0.1-SNAPSHOT-jar-with-dependencies.jar | tee output.log 

