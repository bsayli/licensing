package com.c9.licensing.sdk.cli;

import java.util.concurrent.Callable;

import com.c9.licensing.sdk.cli.config.LicenseSdkClientConfig;
import com.c9.licensing.sdk.cli.service.LicenseSdkClientService;
import com.c9.licensing.sdk.cli.service.impl.LicenseSdkClientServiceImpl;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "licenseSdkCli", mixinStandardHelpOptions = true, version = "1.0", description = "Licensing Sdk Cli")
public class LicenseSdkCli implements Callable<Integer> {

	// Optional environment variable
	private static final String ENV_VAR_LICENSE_KEY = "LICENSE_KEY";
	private static final String ENV_VAR_SERVICE_ID = "SERVICE_ID";
	private static final String ENV_VAR_SERVICE_VERSION = "SERVICE_VERSION";
	private static final String ENV_VAR_INSTANCE_ID = "INSTANCE_ID";

	@Option(names = { "-k", "--key" }, description = "License Key")
	private String licenseKey;

	@Option(names = { "-s", "--service-id" }, description = "Service Id")
	private String serviceId;

	@Option(names = { "-v", "--service-version" }, description = "Service Version")
	private String serviceVersion;

	@Option(names = { "-i", "--instance-id" }, description = "Instance Id")
	private String instanceId;

	public LicenseSdkCli() {
		licenseKey = getEnvOrOption(licenseKey, ENV_VAR_LICENSE_KEY);
		serviceId = getEnvOrOption(serviceId, ENV_VAR_SERVICE_ID);
		serviceVersion = getEnvOrOption(serviceVersion, ENV_VAR_SERVICE_VERSION);
		instanceId = getEnvOrOption(instanceId, ENV_VAR_INSTANCE_ID);
	}

	private String getEnvOrOption(String optionValue, String envVarName) {
		String value = optionValue != null ? optionValue : System.getenv(envVarName);
		if (value == null) {
	        throw new IllegalArgumentException(envVarName + " is required");
	    }
		return value;
	}

	public static void main(String[] args) {
		int exitCode = new CommandLine(new LicenseSdkCli()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		LicenseSdkClientConfig clientConfig = new LicenseSdkClientConfig();
		LicenseSdkClientService clientService = new LicenseSdkClientServiceImpl(clientConfig.getClientProperties());
		return clientService.validateLicense(instanceId, licenseKey, serviceId, serviceVersion);
	}

}
