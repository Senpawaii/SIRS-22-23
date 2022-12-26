import requests
import json
import configparser
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

    # Retrieve fields for frontoffice
    address = config['frontoffice']['ip_address']
    port = config['frontoffice']['port']

    print("Contacting Front office on address " + address + " and port: " + port + "...")
    
    response = sendRequest(address, port)
    print(response)


def sendRequest(address, port):
    URL = "https://"+ address + ":" + port
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/46.0.2490.80'
    }

    try:
        response = requests.get(url=URL, headers=headers, timeout=5)
    except Exception as ex:
        template = "An exception of type {0} occurred. Arguments:\n{1!r}"
        message = template.format(type(ex).__name__, ex.args)
        print(message)
        return message

    json_response = json.loads(response.text)

    # data = response.json()
    for line in json_response:
        print(line)

    # 
    # reply = json_response
    # print(reply)
