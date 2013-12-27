@ECHO OFF
mvn -DskipTests -o package jboss-as:deploy
pause
