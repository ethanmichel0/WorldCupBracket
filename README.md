# How to Use

For local development, please follow the following steps:

1. Clone repo
2. Head to [football api](https://www.api-football.com) and create a free account. Next, create a secrets.properties file in your /backend/src/main/resources folder next to application.properties and enter ```secrets.football-api-key={ENTER YOUR API KEY FROM RAPIDSPORTSAPIHERE}```. This will allow you to make requests to get live world cup results and fixtures. 
3. Create a google oauth client, and enter credentials in teh same secrets.properties file created in step above using credentials: ```spring.security.oauth2.client.registration.google.client-id={YOUR CLIENT ID}
spring.security.oauth2.client.registration.google.client-secret={YOUR CLIENT SECRET}```
3. Download [docker compose](https://docs.docker.com/compose/install/) (free for non enterprise use)
4. In terminal: ``` docker-compose up --build ```  -- This will build your frontend and backend containers and MongoDB database
5. If you make a change and want to see it live on the backend without restarting the docker container, enter the backend docker container with ```docker exec -it worldcupbracket_spring-boot_1 /bin/sh``` in a new terminal window. This will take you inside the backend container. Note that all of the changes you make locally in /backend/src are mounted into the docker container in live time. You will only need to type ```mvn compile``` to recompile your changes from Kotlin files to java class files. Frontend changes should reflect automatically.
7. Backend is on port 6868 and frontend is on port 1234 oof your localhost. Mongodb database is on port 27017. To view your database, you can either download a mongodb visual client locally and connect to port 27017 (database name is "test") or you can use ```docker exec -it worldcupbracket_mongodb_1``` to enter mongodb docker container and run ```mongosh``` within that terminal to view your database if you don't want to download a mongodb client locally. 
