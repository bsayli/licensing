#Welcome to the Licensing Project!

#This repository holds the source code for a licensing system containing four projects:

* license-generator: This is a java project which has license key generation, encryption samples.

* licensing-service: This web project handles core licensing functionalities.

* licensing-service-sdk: This web project provides an SDK for integrating licensing features into other applications.

* licensing-service-sdk-cli: This Java executable is a command-line tool for testing and interacting with the licensing
  service.

#This repository also keeps three main folders:

* db: Keycloak db in a zip format (docker_volumes.zip)

* docker-compose: Keeps the docker-compose.yml files to run servers and client

* scripts: To run the client with sh script (run_license_sdk_cli.sh)

#Prerequisites

* Git client installed on your system.
* Docker installed and running.
* Docker Compose installed and running.

#Setting Up the Environment

1. Clone the Full Repository:

git clone https://github.com/bsayli/licensing.git

2. Extract KeyCloak DB:

    - Get the docker_volumes.zip file from /licensing/db directory.

    - Copy and Extract docker_volumes.zip file into your home directory ($HOME).

#Running the Licensing Service

1. Navigate to Server Docker Compose:

    - cd ../licensing/docker-compose/server

2. Start the Server Components:

    - docker-compose up -d

This will start the Keycloak server, Licensing Service, and Licensing Service SDK containers in the background.

3. Wait for Services to Start:

Allow approximately 45 seconds for the services to fully initialize in the first run.

#Running the License Validation Tool By Docker

1. Navigate to Client Docker Compose:

    - cd ../client

2. Start the Client Service:

    - docker-compose up

This will start the Licensing Service SDK CLI container.

3. Verify License Validation:

    - You should see logs indicating successful license validation.
    - Look for messages similar to:

   licensing-service-sdk-cli | 12:36:54.220 [main] INFO c.c.l.s.c.s.i.LicenseSdkClientServiceImpl - License validated
   successfully:
   licensing-service-sdk-cli | 12:36:54.222 [main] INFO c.c.l.s.c.s.i.LicenseSdkClientServiceImpl - {
   "success" : true,
   "status" : "LICENSE_ACTIVE",
   "message" : "License is active",
   "errorDetails" : null
   }
   licensing-service-sdk-cli exited with code 0

4. Stopping And Removing Containers:

   Client:

    - docker-compose down

   Server:

    - cd ../server
    - docker-compose down

#Running the License Validation Tool (Directly) Optional 1

Building the JAR

The licensing-service-sdk-cli application is distributed as an executable jar file. To build the jar, you'll need to
have Maven installed on your system.

Prerequisites

* Maven (> 3.x)

**Steps:**

1. Clone or download the project repository.
2. Navigate to the project directory. (../licensing/licensing-service-sdk-cli/)
3. Run the following command to build and package the application:

    - mvn clean package

4. The jar file will be inside the directory path ../licensing/licensing-service-sdk-cli/target

Now you can run the licensing-service-sdk-cli jar directly to validate a license:

	- java -jar licensing-service-sdk-cli.jar -k <licenseKey> -s <serviceId> -v <serviceVersion> -i <instanceId>

Required parameters:

-k, --key: License key
-s, --service-id: Service ID
-v, --service-version: Service version
-i, --instance-id: Instance ID

4. How to run:

    - cd ../licensing/licensing-service-sdk-cli/target

    - java -jar licensing-service-sdk-cli-1.0.1.jar -s c9ineCodegen -v 1.2.2 -i c9ineCodegen~macbookuynjkl5~00:2A:8D:BE:
      F1:56 -k
      v6ZFWUUUDlVaONpVJzzDowezuCkCk6szc4ClvB0ow6V+oyuY2bsJCPdVQErI0F7jiJ44X9xoyRCrMN2Ugz2iK1kekvRkHQdaxREMz8NnQCCIodstpdYqSv+h1lNJqROPzfvj23TxHBSKr0PzlS/OoqulJuHb0rU+9WR/LoAFAr5/L740bToGooZ/KLRKKeGOS3LCJfOApMCVvL9YblYxwPPLTOZC2A==

#Running the License Validation Tool with the run_license_sdk_cli.sh Script Optional 2

Prerequisites

* Java installed on the system  (>= 17.x)

1. Navigate to Script Location:

    - cd licensing/scripts/

2. Making the Script Executable:

    - chmod +x run_license_sdk_cli.sh

3. Script Usage:

    - ./run_license_sdk_cli.sh -k <licenseKey> -s <serviceId> -v <serviceVersion> -i <instanceId>

**Options:**

* `-k`: Your license key
* `-s`: The service ID
* `-v`: The service version
* `-i`: The instance ID

4. How to run it with passing options:

    - ./run_license_sdk_cli.sh -s c9ineCodegen -v 1.2.2 -i c9ineCodegen~macbookuynjkl5~00:2A:8D:BE:F1:56 -k
      v6ZFWUUUDlVaONpVJzzDowezuCkCk6szc4ClvB0ow6V+oyuY2bsJCPdVQErI0F7jiJ44X9xoyRCrMN2Ugz2iK1kekvRkHQdaxREMz8NnQCCIodstpdYqSv+h1lNJqROPzfvj23TxHBSKr0PzlS/OoqulJuHb0rU+9WR/LoAFAr5/L740bToGooZ/KLRKKeGOS3LCJfOApMCVvL9YblYxwPPLTOZC2A==


