import random
import requests
import time

from generator import Generator

URL = "http://127.0.0.1:8080"
ENDPOINT = "/unfollow"

# frequency of sending in seconds
INTERVAL = 5

PRINT_PREFIX = "===== UNFOLLOW ===== "

gen = Generator()

while(True):

    # Generate data
    # List of json objects to send
    msg = []
    for _ in range(random.randint(1, 10)):
        record = gen.generate_relation(random.randint(0,1000), random.randint(0,1000))

        # Create a JSON from record
        fields = record.split(",")

        msg_dict = {
            "first": record.split(",")[0],
            "second": record.split(",")[1]
        }

        msg.append(msg_dict)

    print(PRINT_PREFIX, msg)
    
    # Put data
    response = requests.put(f"{URL}{ENDPOINT}", json=msg)
    print(PRINT_PREFIX, f"RESPONSE: {response.content}")

    time.sleep(INTERVAL)