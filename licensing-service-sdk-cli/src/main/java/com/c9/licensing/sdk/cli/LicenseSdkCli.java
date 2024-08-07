package com.c9.licensing.sdk.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.fluent.Request;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "licenseSdkCli", mixinStandardHelpOptions = true, version = "1.0", description = "Licensing Sdk Cli")
public class LicenseSdkCli implements Callable<Integer> {

	private static final String CONFIG_FILE = "application.properties";
	private static final Logger logger = LoggerFactory.getLogger(LicenseSdkCli.class);

	private String sdkBaseUrl;
	private String sdkUsr;
	private String sdkPass;

	@Option(names = { "-k", "--key" }, description = "License Key")
	private String licenseKey;

	@Option(names = { "-s", "--service-id" }, description = "Service Id")
	private String serviceId;

	@Option(names = { "-v", "--service-version" }, description = "Service Version")
	private String serviceVersion;

	@Option(names = { "-i", "--instance-id" }, description = "Instance Id")
	private String instanceId;

	public static void main(String[] args) {
		int exitCode = new CommandLine(new LicenseSdkCli()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		loadConfiguration();
		String url = sdkBaseUrl + "/api/license/validate";

		CloseableHttpClient httpClient = buildHttpClient();
		Request postRequest = buildPostRequest(url);

		return callAndHandleResponse(httpClient, postRequest);
	}

	private Integer callAndHandleResponse(CloseableHttpClient httpClient, Request postRequest) throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		try {
			String response = postRequest.execute(httpClient).returnContent().toString();
			LicenseValidationResponse licenseResponse = mapper.readValue(response, LicenseValidationResponse.class);
			String licenseResponsePrettyStr = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(licenseResponse);
			logger.info("License validated successfully:");
			logger.info(licenseResponsePrettyStr);
			return 0;
		} catch (HttpResponseException e) {
			int statusCode = e.getStatusCode();
			if (isServerError(statusCode, e.getContentType())) {
				LicenseValidationResponse errorResponse = parseErrorResponse(e, mapper);
				String licenseErrorResponsePrettyStr = mapper.writerWithDefaultPrettyPrinter()
						.writeValueAsString(errorResponse);
				logger.error("License validatation failed with status code {}", statusCode);
				logger.error(licenseErrorResponsePrettyStr);
				return 1;
			} else {
				logger.error("License validatation failed with status code {}", statusCode);
				return 1;
			}
		} catch (Exception e) {
			logger.error("An unexpected error occured:", e);
			return 1;
		} finally {
			httpClient.close();
		}
	}

	private CloseableHttpClient buildHttpClient() {
		return HttpClients.custom()
				.setRetryStrategy(new DefaultHttpRequestRetryStrategy(3, TimeValue.of(3, TimeUnit.SECONDS)))
				.build();
	}

	private Request buildPostRequest(String url) {
		return Request.post(url)
				.connectTimeout(Timeout.of(40, TimeUnit.SECONDS))
				.responseTimeout(Timeout.of(40, TimeUnit.SECONDS))
				.setHeader(HttpHeaders.AUTHORIZATION, createBasicAuthHeader(sdkUsr, sdkPass))
				.addHeader("Content-Type", ContentType.APPLICATION_FORM_URLENCODED.toString())
				.addHeader("Accept", ContentType.APPLICATION_JSON.toString())
				.addHeader("X-Instance-ID", instanceId)
				.bodyForm(new BasicNameValuePair("licenseKey", licenseKey),
						new BasicNameValuePair("serviceId", serviceId),
						new BasicNameValuePair("serviceVersion", serviceVersion));
	}

	private boolean isServerError(int statusCode, ContentType contentType) {
		List<Integer> validStatusCodes = List.of(HttpStatus.SC_BAD_REQUEST, HttpStatus.SC_UNAUTHORIZED,
				HttpStatus.SC_INTERNAL_SERVER_ERROR);
		return validStatusCodes.contains(statusCode) && ContentType.APPLICATION_JSON.isSameMimeType(contentType);
	}

	private LicenseValidationResponse parseErrorResponse(HttpResponseException e, ObjectMapper mapper)
			throws IOException {
		return mapper.readValue(e.getContentBytes(), LicenseValidationResponse.class);
	}

	private String createBasicAuthHeader(String appUser, String appPass) {
		String authString = appUser + ":" + appPass;
		byte[] authEncBytes = Base64.getEncoder().encode(authString.getBytes());
		return "Basic " + new String(authEncBytes);
	}

	private void loadConfiguration() throws IOException {
		Properties properties = new Properties();
		try (InputStream input = LicenseSdkCli.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
			properties.load(input);
		}
		sdkBaseUrl = properties.getProperty("licensing.sdk.server.base.url");
		sdkUsr = properties.getProperty("licensing.sdk.server.app.user");
		sdkPass = properties.getProperty("licensing.sdk.server.app.pass");
	}

}
