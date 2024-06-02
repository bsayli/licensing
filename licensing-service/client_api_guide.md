# Client Guide to Licensing Endpoints

This document explains how client applications can interact with the licensing service. The service provides two main functionalities:

Initial License Validation: This step should be used by clients only one time to validate license key.
License Validation with Token: This step is used by clients after successful initial validation to validate their license using a previously obtained token.

Client Requirements:

All requests to the licensing service require basic authentication with a username and password (details on how to configure basic authentication are omitted for brevity).
Clients need to include the X-App-Instance-ID header in all requests. This header should contain a unique identifier for the client machine.

Endpoints:
1. /api/license/validate
This endpoint is used for initial license validation.
* Request:
    * Method: POST
    * Body: licenseKey parameter containing the client's license key.
    * Headers:
        * X-App-Instance-ID: Unique client machine identifier.
* Response:
    * Status code:
        * 200 (OK): Validation successful. Response body contains a LicenseValidationResponse object with:
            * success: True
            * token: A JWT token containing license information.
            * message: Validation message (optional).
        * 401 (Unauthorized): Validation failed. Response body contains a LicenseValidationResponse object with:
            * success: False
            * errorCode: Error code describing the validation failure (e.g., INVALID_LICENSE_KEY).
            * message: Error message.
2. /api/license/validateToken
This endpoint is used for subsequent license validation using a previously obtained token.
* Request:
    * Method: POST
    * Headers:
        * X-License-Token: License token obtained from the /api/license/validate endpoint.
        * X-App-Instance-ID: Unique client machine identifier.
* Response:
    * Status code:
        * 200 (OK): Validation successful. Response body contains a LicenseValidationResponse object with:
            * success: True
            * token: (Optional) A new token if the previous token was refreshed. Otherwise, the original token is returned.
            * errorCode: (Optional) If the token was refreshed, the error code will be TOKEN_REFRESHED.
            * message: Validation message (optional).
        * 401 (Unauthorized): Validation failed. Response body contains a LicenseValidationResponse object with:
            * success: False
            * errorCode: Error code describing the validation failure (e.g., INVALID_TOKEN).
            * message: Error message.
