recommended reading on how to create and setup authorities for google service account, use of OAuth2:
https://developers.google.com/identity/protocols/OAuth2ServiceAccount
https://developers.google.com/api-client-library/java/google-api-java-client/oauth2


go to page
https://console.cloud.google.com/iam-admin/serviceaccounts/serviceaccounts-zero
create or select a project
create a service account
check the box to generate new private key, type JSON
check the box to delegate access to all the data in domain

go to page
https://developers.google.com/apis-explorer/#p/sheets/v4/
enable OAuth 2.0 for GoogleSheets API

go to page
https://console.developers.google.com/apis/library
enable google sheets api for your project


document that is read, must have read permission given to service account