import requests
import json

def run():
    print("This is the client source code!")
    sendRequest()

def sendRequest():
    URL = "http://localhost:10000"
    
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/46.0.2490.80'
    }

    response = requests.get(url=URL, headers=headers)


    data = response.json()
    print(data)

    # json_response = json.loads(response.text)
    # reply = json_response
    # print(reply)
