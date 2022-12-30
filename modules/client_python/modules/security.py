import re
import getpass
import hashlib

from Crypto.PublicKey import RSA
from Crypto.Cipher import PKCS1_OAEP
from Crypto import Random
from Crypto.Cipher import AES
from cryptography.fernet import Fernet

import base64
import rsa
""" 
    Sanitization module. It is crucial that this sanitization is executed again in the servers.
    Sanitizing data on the client side, allows for the mitigation of unnecessary requests. 
"""

def get_username():
    while(True):
        username = input("Username: ")

        # Check if the username:
        #   - is not empty;
        #   - is not something that is not a string 
        #   - matches upper/lower characters or numbers
        #   - fixed size under or equal 10 characters 
        if not username or not isinstance(username, str) or not re.match(r"^[a-zA-Z0-9]{1,10}$", username):
            print("Invalid username. Please try again.")
        else:
            return username

def get_hash_password():
    # TODO: Add some mechanism to truncate received password size on the SERVER ONLY, THIS APP IS THE CLIENTS PROBLEM IF HE MESSES WITH IT. 
    # Inputing a 32KB password creates considerable lag on the client, let alone the server.
    password = getpass.getpass()

    # Check if the password:
        #   - is not empty;
        #   - is not something that is not a string 
        #   - matches upper/lower characters or numbers and special characters

    return hashlib.sha256(password.encode()).hexdigest()

def encryptRSA(request, key_to_encrypt):
    # Load the RSA key to encrypt the request
    crypt_key = RSA.import_key(key_to_encrypt)

    # Create cipher
    cipher_req = PKCS1_OAEP.new(crypt_key)

    # Encrypt request with the cipher
    encrypted_request = cipher_req.encrypt(request.encode())

    # Improve performance and integrity of message by converting it to hexadecimal string
    return encrypted_request

def get_shared_encrypted_key():
    # # Generate a symmetric Fernet key
    # symmetric_key = Fernet.generate_key()
    # print("Fernet key:" + symmetric_key.decode())
    symmetric_key = Random.new().read(32)
    # iv = Random.new().read(AES.block_size)
    # cipher = AES.new(aeskey, AES.MODE_CFB, iv)

    
    # Read the Server public key
    with open("extra_files/backoffice/public.key", "rb") as f:
        public_key = RSA.import_key(f.read())

    # Create cipher
    # cipher = PKCS1_OAEP.new(public_key) UNCOMMENT

    # Encrypt the symmetric key with the Server public key
    # encrypted_symmetric_key = cipher.encrypt(symmetric_key) UNCOMMENT

    encrypted_symmetric_key = rsa.encrypt(symmetric_key, public_key)
    print("LENs:" + str(len(encrypted_symmetric_key)) + "|||" + str(public_key.n))
    print("=============ENCRYPTED SYMMETRIC KEY =================")
    print(encrypted_symmetric_key)
    print("=============CONVERT BASE 64 STRING SYMMETRIC KEY =================")
    print(base64.b64encode(encrypted_symmetric_key))
    encrypted_symmetric_key_b64 = str(base64.b64encode(encrypted_symmetric_key), 'utf-8')
    return encrypted_symmetric_key_b64, symmetric_key, encrypted_symmetric_key

def encryptAES(data, key):
    # # Create a Fernet cipher
    # cipher = Fernet(key)
    # Encrypt the data using the cipher
    # encrypted_data = cipher.encrypt(data.encode())
    iv = Random.new().read(AES.block_size)
    cipher = AES.new(key, AES.MODE_CFB, iv)
    msg = iv + cipher.encrypt(data.encode())
    print("=============ENCRYPTED MESSAGE =================")
    print(msg)
    print("=============CONVERT BASE 64 STRING MESSAGE=================")
    encrypted_message_b64 = str(base64.b64encode(msg),'utf-8')
    print(encrypted_message_b64)
    return encrypted_message_b64

# def convert_b64(data):
#     # We cannot use binary data in the JSON dict. Convert it to Base-64 first.
#     print("=============BEFORE BASE 64 =================")
#     print(data)
#     print("=============CONVERT BASE 64=================")
#     print(data.encode('base64'))
#     return data.encode('base64')
