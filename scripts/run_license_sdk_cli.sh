#!/bin/bash
set -euo pipefail

# Function to find a suitable Java installation
find_java() {
  if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
    echo "$JAVA_HOME/bin/java"
    return 0
  fi

  local java_locations=(
    "/usr/bin/java"
    "/usr/local/bin/java"
    "/Library/Java/JavaVirtualMachines"/*/Contents/Home/bin/java
  )

  for java_path in "${java_locations[@]}"; do
    if [[ -x "$java_path" ]]; then
      echo "$java_path"
      return 0
    fi
  done

  echo "❌ Could not find a suitable Java installation." >&2
  exit 1
}

# Resolve Java executable
java_bin=$(find_java)

# Locate JAR file (pick the latest if multiple exist)
script_dir=$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )
jar_file=$(ls -1t "$script_dir"/../licensing-service-sdk-cli/target/licensing-service-sdk-cli-*.jar 2>/dev/null | head -n 1 || true)

if [[ -z "$jar_file" ]]; then
  echo "❌ JAR file not found. Did you run 'mvn clean package'?" >&2
  exit 1
fi

# Run CLI
"$java_bin" -jar "$jar_file" "$@"
exit_code=$?

if [[ $exit_code -eq 0 ]]; then
  echo "✅ License validated successfully."
else
  echo "❌ CLI exited with code $exit_code"
fi