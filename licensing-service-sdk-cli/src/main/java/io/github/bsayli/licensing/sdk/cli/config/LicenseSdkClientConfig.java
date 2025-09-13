package io.github.bsayli.licensing.sdk.cli.config;

import io.github.bsayli.licensing.sdk.cli.LicenseSdkCli;
import io.github.bsayli.licensing.sdk.cli.model.LicenseSdkClientProperties;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LicenseSdkClientConfig {

  private static final String CONFIG_FILE = "application.properties";

  // ENV var names
  private static final String ENV_URL = "LICENSE_SERVICE_SDK_URL";
  private static final String ENV_CONNECT = "LICENSE_SERVICE_SDK_CONNECT_TIMEOUT";
  private static final String ENV_RESPONSE = "LICENSE_SERVICE_SDK_RESPONSE_TIMEOUT";
  private static final String ENV_RETRIES = "LICENSE_SERVICE_SDK_RETRIES";
  private static final String ENV_RETRY_INT = "LICENSE_SERVICE_SDK_RETRY_INTERVAL";
  private static final String ENV_PATH = "LICENSE_SERVICE_SDK_API_PATH";

  // Property keys
  private static final String P_URL = "licensing.sdk.server.url";
  private static final String P_USER = "licensing.sdk.server.app.user";
  private static final String P_PASS = "licensing.sdk.server.app.pass";
  private static final String P_PATH = "licensing.sdk.api.path";
  private static final String P_CONNECT = "licensing.sdk.http.connect-timeout-seconds";
  private static final String P_RESPONSE = "licensing.sdk.http.response-timeout-seconds";
  private static final String P_RETRIES = "licensing.sdk.http.retries";
  private static final String P_RETRY_INT = "licensing.sdk.http.retry-interval-seconds";

  // Defaults
  private static final int DEF_CONNECT = 40;
  private static final int DEF_RESPONSE = 40;
  private static final int DEF_RETRIES = 3;
  private static final int DEF_RETRY_INT = 3;
  private static final String DEF_PATH = "/v1/licenses/access";

  private static String firstNonBlank(String a, String b) {
    if (a != null && !a.isBlank()) return a.trim();
    if (b != null && !b.isBlank()) return b.trim();
    return null;
  }

  private static int parseIntOrDefault(String val, int def) {
    if (val == null || val.isBlank()) return def;
    try {
      return Integer.parseInt(val.trim());
    } catch (NumberFormatException e) {
      return def;
    }
  }

  public LicenseSdkClientProperties getClientProperties() throws IOException {
    Properties properties = new Properties();
    try (InputStream input =
        LicenseSdkCli.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
      if (input != null) {
        properties.load(input);
      }
    }

    String baseUrl = firstNonBlank(System.getenv(ENV_URL), properties.getProperty(P_URL));
    String appUser = properties.getProperty(P_USER);
    String appPass = properties.getProperty(P_PASS);

    int connectTimeout =
        parseIntOrDefault(
            firstNonBlank(System.getenv(ENV_CONNECT), properties.getProperty(P_CONNECT)),
            DEF_CONNECT);

    int responseTimeout =
        parseIntOrDefault(
            firstNonBlank(System.getenv(ENV_RESPONSE), properties.getProperty(P_RESPONSE)),
            DEF_RESPONSE);

    int retries =
        parseIntOrDefault(
            firstNonBlank(System.getenv(ENV_RETRIES), properties.getProperty(P_RETRIES)),
            DEF_RETRIES);

    int retryIntervalSeconds =
        parseIntOrDefault(
            firstNonBlank(System.getenv(ENV_RETRY_INT), properties.getProperty(P_RETRY_INT)),
            DEF_RETRY_INT);

    String apiPath = firstNonBlank(System.getenv(ENV_PATH), properties.getProperty(P_PATH));
    if (apiPath == null || apiPath.isBlank()) {
      apiPath = DEF_PATH;
    }

    return new LicenseSdkClientProperties(
        baseUrl,
        appUser,
        appPass,
        apiPath,
        connectTimeout,
        responseTimeout,
        retries,
        retryIntervalSeconds);
  }
}
