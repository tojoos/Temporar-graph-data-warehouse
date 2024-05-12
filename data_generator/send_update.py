import random
import requests
import json
import time

from generator import Generator

URL = "http://127.0.0.1:5000"
ENDPOINT = "/update"

gen = Generator()

while(True):

    # Generate data
    # List of json objects to send
    msg = []
    for _ in range(random.randint(1, 10)):
        record = gen.generate_node(random.randint(0,10000))

        # Create a JSON from record
        fields = record.split(",")

        msg_dict = {
            "id": record.split(",")[0],
            "nickname": record.split(",")[1],
            "sex": record.split(",")[2],
            "nationality": record.split(",")[3]
        }

        # print(json.dumps(msg_dict))
        msg.append(json.dumps(msg_dict))
        
    print(msg) 
    print("-----")

    # ---------- UNCOMMENT ----------
    # Put data
    # response = requests.put(f"{URL}{ENDPOINT}", data=msg)

    time.sleep(3)