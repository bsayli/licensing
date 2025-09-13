package io.github.bsayli.licensing.sdk.cli.config;

import io.github.bsayli.licensing.sdk.cli.LicenseSdkCli;
import io.github.bsayli.licensing.sdk.cli.model.LicenseSdkClientProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LicenseSdkClientConfig {

  private static final String ENV_VAR_LICENSE_SERVICE_SDK_URL = "LICENSE_SERVICE_SDK_URL";
  private static final String CONFIG_FILE = "application.properties";

  public LicenseSdkClientProperties getClientProperties() throws IOException {
    Properties properties = new Properties();
    try (InputStream input =
        LicenseSdkCli.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      properties.load(input);
    }

    String baseUrl = System.getenv(ENV_VAR_LICENSE_SERVICE_SDK_URL);
    if (baseUrl == null) {
      baseUrl = properties.getProperty("licensing.sdk.server.url");
    }

    return new LicenseSdkClientProperties(
        baseUrl,
        properties.getProperty("licensing.sdk.server.app.user"),
        properties.getProperty("licensing.sdk.server.app.pass"));
  }
}
