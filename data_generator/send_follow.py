import random
import requests
import json
import time

from generator import Generator

URL = "http://127.0.0.1:8080"
ENDPOINT = "/follow"

gen = Generator()

while(True):

    # Generate data
    # List of json objects to send
    msg = []
    for _ in range(random.randint(1, 10)):
        record = gen.generate_relation(random.randint(0,10000), random.randint(0,10000))

        # Create a JSON from record
        fields = record.split(",")

        msg_dict = {
            "first": record.split(",")[0],
            "second": record.split(",")[1]
        }

        # print(json.dumps(msg_dict))
        msg.append(json.dumps(msg_dict))
        
    print(msg) 
    print("-----")

    # ---------- UNCOMMENT ----------
    # Put data
    response = requests.put(f"{URL}{ENDPOINT}", data=msg)

    time.sleep(3)