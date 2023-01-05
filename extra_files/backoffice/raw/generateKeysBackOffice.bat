@echo off

set certCN=localhost

echo Generating JKS Keystore...
keytool -genkeypair -alias %certCN% -keyalg RSA -keysize 2048 -keystore backoffice.jks -dname "cn=%certCN%, ou=My Organization, o=My Organization, l=My City, s=My State, c=My Country" -storepass backoffice -keypass backoffice
keytool -selfcert -alias %certCN% -keystore backoffice.jks -storepass backoffice -keypass backoffice -validity 365

echo Extracting Private Key...
keytool -importkeystore -srckeystore backoffice.jks -srcalias %certCN% -destkeystore privatekey.p12 -deststoretype PKCS12 -srcstorepass backoffice -deststorepass backoffice
openssl pkcs12 -in privatekey.p12 -out privatekey.pem  -nocerts -nodes -passin pass:backoffice

echo Extracting Certificate...
keytool -export -alias %certCN% -keystore backoffice.jks -storepass backoffice -rfc -file BackOfficeCertificate.pem

echo Extracting Public Key...
openssl x509 -in BackOfficeCertificate.pem -pubkey -noout > BackOfficePublicKey.pem