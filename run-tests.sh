#!/bin/bash
# Start run-devt.sh before running 

echo "Start run-devt.sh separately before running"

mvn clean flyway:clean $1 $2 
mvn antrun:run flyway:migrate flyway:info -Dprofile=test -Punit-tests $1 $2 
mvn -Dprofile=test -Punit-tests clean kotlin:compile kotlin:test-compile test surefire:test | tee test-output.log 
#-Dit.test=rev.gretty.homerest.suite.test,BankAccount*Suite verify | tee test-output.log

# Statistics:~/workspace/homerest$ find src/ -type f -name \*.java -o -name \*.groovy -o -name \*.kt | xargs  cat | wc -l
#$ 5143 
