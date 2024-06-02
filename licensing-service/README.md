# Licensing Service - POC

This project provides a Proof-of-Concept (POC) implementation of a licensing service. It utilizes Docker containers for a self-contained and easy-to-run environment.

Prerequisites:
* Docker Engine installed and running on your system. You can find installation instructions on the official Docker website: https://www.docker.com/products/docker-desktop/

* Docker Compose installed and running on your system. You can find installation instructions on the official Docker Compose website: https://docs.docker.com/compose/install/

Project Structure:
* Dockerfile: Defines the Docker image for the licensing service application.

* docker-compose.yml: Configures the services required to run the application, including the Keycloak database.

* poc-resources (folder): Contains additional resources for the POC:
    * docker_keyclock_volumes.zip: Archive containing Keycloak database volumes.
    * licensing-service.postman_collection.json.zip: Exported Postman collection for API interaction.
    
* client_api_guide.md: Explains how client applications can interact with the licensing service endpoints.

Running the Project:

* Extract Resources: Extract the contents of both zip files located in the poc-resources folder. You can use a tool like unzip for extraction.


* Prepare Keycloak Data Volume:
The docker-compose.yml file might define a volume mount for the Keycloak database. This volume maps a directory on your host machine to a location within the container where Keycloak stores its data. The default configuration in docker-compose.yml might specify a volume mount like: 

volumes:
  ${HOME}/docker_volumes/keycloak:/opt/keycloak/data
  
This configuration maps the directory ${HOME}/docker_volumes/keycloak on your host machine to the /opt/keycloak/data directory within the Keycloak container.Extract the contents of docker_keyclock_volumes.zip into the ${HOME} directory. (docker_keyclock_volumes.zip already has docker_volumes root folder and related subfolders)


* Start the Services: Open a terminal in the project's root directory and run the following command:

	docker-compose up -d

This command uses Docker Compose to build the Docker image (based on the Dockerfile) and start the required services defined in docker-compose.yml. The -d flag instructs Docker Compose to run the services in detached mode, allowing them to run in the background.

* Access the Service: The specific way to access the service depends on its implementation. The Postman collection (licensing-service.postman_collection.json) can be imported into Postman to explore and interact with the service's API endpoints. 

* Stop and Remove Services: To stop the running services, use the following command:

	docker-compose down

This will gracefully stop the running containers and associated networks.
