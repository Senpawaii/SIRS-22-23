@echo off

set certCN=localhost

echo Generating JKS Keystore...
keytool -genkeypair -alias %certCN% -keyalg RSA -keysize 2048 -keystore frontoffice.jks -dname "cn=%certCN%, ou=My Organization, o=My Organization, l=My City, s=My State, c=My Country" -storepass frontoffice -keypass frontoffice
keytool -selfcert -alias %certCN% -keystore frontoffice.jks -storepass frontoffice -keypass frontoffice -validity 365

echo Extracting Private Key...
keytool -importkeystore -srckeystore frontoffice.jks -srcalias %certCN% -destkeystore privatekey.p12 -deststoretype PKCS12 -srcstorepass frontoffice -deststorepass frontoffice
openssl pkcs12 -in privatekey.p12 -out privatekey.pem  -nocerts -nodes -passin pass:frontoffice

echo Extracting Certificate...
keytool -export -alias %certCN% -keystore frontoffice.jks -storepass frontoffice -rfc -file FrontOfficeCertificate.pem

echo Extracting Public Key...
openssl x509 -in FrontOfficeCertificate.pem -pubkey -noout > FrontOfficePublicKey.pem