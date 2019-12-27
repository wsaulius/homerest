#!/bin/bash
mvn -Dprofile=test -Punit-tests clean kotlin:compile kotlin:test-compile surefire:test 
