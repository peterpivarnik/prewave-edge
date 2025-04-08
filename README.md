### Run instructions

Below is an example of how you can run this demo.

0. Prerequisities:
   make sure you have installed git, maven docker and java and kotlin on your machine
1. Navigate to the project root folder
2. Pull postgres docker image
    ```sh
    docker pull postgres
   ```
3. Run postgres in docker image
    ```sh
    docker run --publish 5432:5432 --name postgresDb -e POSTGRES_PASSWORD=edgepassword -e POSTGRES_USER=edgeuser -e POSTGRES_DB=edge -d postgres
   ```
4. Build project
   ```sh
   mvn clean install
   ```   
5. Run application
   Run by running main method in EdgeApplication.kt
6. Check in browser if application is correctly started
   ```
   localhost:8080/actuator/health
   ```
7. Test application by sending requests from postman or from resource file src/test/resources/test.http

List of endpoints:
POST http://localhost:8080/edge creates edge
GET http://localhost:8080/edge list all edges
GET http://localhost:8080/edge/{edgeId) gets edge by edgeId
DELETE http://localhost:8080/edge/{edgeId} delete edge by edgeId
GET http://localhost:8080/tree/{fromId} return tree of edges with root edge having provided fromId

