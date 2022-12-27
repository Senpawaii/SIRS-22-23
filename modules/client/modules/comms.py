import requests
import json
import configparser
import ssl
import socket

import os # TODO: only debug

# TODO: Include logging capabilities: import logging

def contactBackoffice():
    # Load properties file
    config = configparser.ConfigParser()
    config.read("./resources/app.properties")

    # Retrieve fields for backoffice
    address = config['backoffice']['ip_address']
    port = config['backoffice']['port']

    print("Contacting Back office on address " + address + " and port: " + port + "...")
    
    response = sendRequest(address, port)
    print(response)

def contactFrontoffice():
    # Load properties file
    config = configparser.ConfigParser()
    config.read("./resources/app.properties")
    print(os.getcwd())
    # Retrieve fields for frontoffice
    address = config['frontoffice']['ip_address']
    port = config['frontoffice']['port']

    print("Contacting Front office on address " + address + " and port: " + port + "...")
    
    response = sendRequest(address, port)
    print(response)


def sendRequest(address, port, request, ciphers, type_req, handler):
    URL = "https://"+ address + ":" + port + handler
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/46.0.2490.80'
    }
    
    match type_req:
        case "POST":
            try:
                print(os.getcwd())
                response = requests.post(url=URL, headers=headers, timeout=10000, json=request, verify="./extra_files/backoffice/server.crt")
                return json.loads(response.text)
            except Exception as ex:
                    template = "An exception of type {0} occurred. Arguments:\n{1!r}"
                    message = template.format(type(ex).__name__, ex.args)
                    print(message)
                    return message
        case Any:
            # // TODO: Handle error!
            return 


def connect_to_backoffice(request):
    # Load properties file
    config = configparser.ConfigParser()
    config.read("modules/client/resources/app.properties")
    print(os.getcwd())
    # Retrieve fields for backoffice
    address = config['backoffice']['ip_address']
    port = config['backoffice']['port']
    
    print("Contacting Back office on address " + address + " and port: " + port + "...")
    
    ciphers = "ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256"

    return sendRequest(address, port, request, ciphers, "POST", "/auth")


def verify_credentials(username, hashed_password):
    request = {
        "type":"verify_auth",
        "username":username,
        "hash_password":hashed_password
    }
    json_request = json.dumps(request)

    # print(json.loads(json_request))
    response = connect_to_backoffice(json_request)

    print(response)
    return False # TODO: Parse response here and return Boolean according to the answer
    
    
