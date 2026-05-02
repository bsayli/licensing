package io.github.bsayli.licensing.agent.cli;

import io.github.bsayli.licensing.agent.cli.config.LicenseAgentClientConfig;
import io.github.bsayli.licensing.agent.cli.service.LicenseAgentClientService;
import io.github.bsayli.licensing.agent.cli.service.impl.LicenseAgentClientServiceImpl;
import java.util.concurrent.Callable;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
    name = "licensingAgentCli",
    mixinStandardHelpOptions = true,
    version = "1.0",
    description = "Licensing Agent Cli")
public class LicenseAgentCli implements Callable<Integer> {

  @Option(
      names = {"-k", "--key"},
      description = "License Key",
      required = true,
      defaultValue = "${env:LICENSE_KEY}")
  private String licenseKey;

  @Option(
      names = {"-s", "--service-id"},
      description = "Service Id",
      required = true,
      defaultValue = "${env:SERVICE_ID}")
  private String serviceId;

  @Option(
      names = {"-v", "--service-version"},
      description = "Service Version",
      required = true,
      defaultValue = "${env:SERVICE_VERSION}")
  private String serviceVersion;

  @Option(
      names = {"-i", "--instance-id"},
      description = "Instance Id",
      required = true,
      defaultValue = "${env:INSTANCE_ID}")
  private String instanceId;

  public static void main(String[] args) {
    int exitCode = new CommandLine(new LicenseAgentCli()).execute(args);
    System.exit(exitCode);
  }

  @Override
  public Integer call() throws Exception {
    LicenseAgentClientConfig clientConfig = new LicenseAgentClientConfig();
    LicenseAgentClientService clientService =
        new LicenseAgentClientServiceImpl(clientConfig.getClientProperties());
    return clientService.validateLicense(instanceId, licenseKey, serviceId, serviceVersion);
  }
}
