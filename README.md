# Sales Kafka - Saga Patterns
Monorepo of a project implementing saga patterns with Java Spring Boot and Kafka.

## Technologies
* **Java 17**
* **Spring Boot 3**
* **Gradle**
* **Apache Kafka**
* **API REST**
* **PostgreSQL**
* **MongoDB**
* **Docker**
* **Redpanda Console**

# Orchestrated Saga Architecture
**Branch: master**
![image](https://github.com/user-attachments/assets/f3cb1a19-8ea4-4d3c-8356-a2f157f6e0aa)

* **Order-Service**: microservice responsible only for generating an initial request and receiving a notification. Here we have REST endpoints to start the process and retrieve event data. The database used will be MongoDB.
* **Orchestrator-Service**: microservice responsible for orchestrating the entire Saga execution flow, it will know which microservice was executed and in which state, and which will be the next microservice to be sent, this microservice will also save the process from events. This service does not have a database.
* **Product-Validation-Service**: microservice responsible for validating whether the product specified in the order exists and is valid. This microservice will store a product validation for an order ID. The database used will be PostgreSQL.
* **Payment-Service**: microservice responsible for making a payment based on the unit values ​​and quantities informed in the order. This microservice will store the payment information for an order. The database used will be PostgreSQL.
* **Inventory-Service**: microservice responsible for lowering the stock of products from an order. This microservice will store the download information of a product for an order ID. The database used will be PostgreSQL.

All services will go up through **docker-compose.yml**.

## How to run
There are several ways to run:
1. Running everything via `docker-compose`
2. Running everything through the automation `script` that i made available (`build.py`)
3. Running only the database and message broker (Kafka) services separately
4. Running applications manually via CLI (`java -jar` or `gradle bootRun` or via IntelliJ)

To run the applications, you will need to have installed:

* **Docker**
* **Java 17**
* **Gradle 7.6 or higher**

