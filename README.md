# Orchestrator
Orchestrator for receiving tweets from Kafka and apply sentiment analysis.

## How to run
### Local
1. Clone this project to a local folder and go to root folder

   `git clone hhttps://github.com/twittersentimentanalysis/Orchestrator.git`

2. Build the Spring Boot project with Maven

    `mvn clean install`
    
3. Generate jar file to execute the project

    `mvn package`

4. Run the project

    `mvn exec:java -Dexec.mainClass=Orchestrator`
    