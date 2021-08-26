# Orchestrator
Orchestrator for receiving tweets from Kafka and apply sentiment analysis.

## Documentation
https://twittersentimentanalysis.github.io/Orchestrator/javadoc/index.html


## How to run
### Local
1. Clone this project to a local folder and go to root folder

   `git clone https://github.com/twittersentimentanalysis/Orchestrator.git`

2. Build the Spring Boot project with Maven

    `mvn clean install`
    
3. Generate jar file to execute the project

    `mvn package`

4. Run the project

    `mvn exec:java -Dexec.mainClass=Orchestrator`



***Note: File `src/main/resources/config.properties` can be modified at anytime to point to localhost or gessi endpoint.*
