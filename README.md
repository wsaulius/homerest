# HomeREST

Home REST app is a Proof of Concept application for testing 
serverless or webapp deployment of the same code. 

It was successfully launched as a tomcat9x application as well
as a gretty instance: embedded jetty standalone server. 

For persistence it relies Hibernate ORM for H2 or MySQL databases. 

    The POC shows how to call REST testing from different sides: 
    from API endpoints and from JUnit tests. The tests are written 
    in JUnit5 and Java. Some Kotlin testing base is introduced. 

* Jetty embedded
* Hibernate 
* Servlets

* Guice  
* Log4j 

* Java 
* Groovy 
* Kotlin 
* Maven

* Transactional 
* Concurrent 

* Secure logins (todo) 
* Ehcache (todo) 
* Javadoc (todo)



starting as ./run-test or ./run-prod.sh 

http://localhost:8080/get/accounts/
http://localhost:8080/get/transactions/
http://localhost:8080/homerest/accounts
http://localhost:8080/homerest/transactions


 by Saulius Veinšreider (@wsaulius)
 
 