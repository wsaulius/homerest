#!/bin/bash

mvn clean kotlin:compile kotlin:test-compile install -Dmaven.repo.local=./localrepo/ 

