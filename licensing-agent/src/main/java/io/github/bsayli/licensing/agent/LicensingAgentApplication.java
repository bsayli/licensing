package io.github.bsayli.licensing.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = "io.github.bsayli.licensing")
@ComponentScan(basePackages = "io.github.bsayli.licensing")
public class LicensingAgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(LicensingAgentApplication.class, args);
    }
}
