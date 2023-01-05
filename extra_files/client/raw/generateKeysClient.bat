@echo off

set certCN=localhost
set entity=client
set entityName=Client

echo Generating JKS Keystore...
keytool -genkeypair -alias %certCN% -keyalg RSA -keysize 2048 -keystore %entity%.jks -dname "cn=%certCN%, ou=My Organization, o=My Organization, l=My City, s=My State, c=My Country" -storepass %entity% -keypass %entity%
keytool -selfcert -alias %certCN% -keystore %entity%.jks -storepass %entity% -keypass %entity% -validity 365

echo Extracting Private Key...
keytool -importkeystore -srckeystore %entity%.jks -srcalias %certCN% -destkeystore privatekey.p12 -deststoretype PKCS12 -srcstorepass %entity% -deststorepass %entity%
openssl pkcs12 -in privatekey.p12 -out privatekey.pem  -nocerts -nodes -passin pass:%entity%

echo Extracting Certificate...
keytool -export -alias %certCN% -keystore %entity%.jks -storepass %entity% -rfc -file %entityName%Certificate.pem

echo Extracting Public Key...
openssl x509 -in %entityName%Certificate.pem -pubkey -noout > %entityName%PublicKey.pem