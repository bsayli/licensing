package io.github.bsayli.licensing.sdk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "io.github.bsayli.licensing")
public class LicenseSdkApplication {

  public static void main(String[] args) {
    SpringApplication.run(LicenseSdkApplication.class, args);
  }
}
