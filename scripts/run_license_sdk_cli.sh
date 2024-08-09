#!/bin/bash

# Function to find a suitable Java installation
function find_java() {
  # List of potential Java locations
  java_locations=(
    "/usr/bin/java"
    "/usr/local/bin/java"
    "/Library/Java/JavaVirtualMachines/*/Contents/Home/bin/java"
  )

  for java_path in "${java_locations[@]}"; do
    if [[ -x "$java_path" ]]; then
      echo "$java_path"
      return 0
    fi
  done

  echo "Could not find a suitable Java installation."
  exit 1
}

# Find the Java executable
java=$(find_java)

# Path to the JAR file
script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
jar_file="$script_dir/../licensing-service-sdk-cli/target/licensing-service-sdk-cli-1.0.1.jar"

$java -jar "$jar_file" "$@"

# Check the exit code of the JAR
exit_code=$?

if [ $exit_code -eq 0 ]; then
  echo "License validated successfully."
else
  echo "Error executing JAR file. Exit code: $exit_code"
fi

