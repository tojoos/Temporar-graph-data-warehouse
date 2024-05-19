import random
import requests
import time

from generator import Generator

URL = "http://127.0.0.1:8080"
ENDPOINT = "/update"

# frequency of sending in seconds
INTERVAL = 10

PRINT_PREFIX = "===== UPDATE ===== "
gen = Generator()

while(True):

    # Generate data
    # List of json objects to send
    msg = []
    for _ in range(random.randint(1, 10)):
        record = gen.generate_node(random.randint(0,1000))

        # Create a JSON from record
        fields = record.split(",")

        msg_dict = {
            "id": record.split(",")[0],
            "nickname": record.split(",")[1],
            "sex": record.split(",")[2],
            "nationality": record.split(",")[3]
        }

        msg.append(msg_dict)

    print(PRINT_PREFIX, msg)
    
    # Put data
    response = requests.put(f"{URL}{ENDPOINT}", json=msg)
    print(PRINT_PREFIX, f"RESPONSE: {response.content}")

    time.sleep(INTERVAL)