import random
import requests

from generator import Generator

URL = "http://127.0.0.1:8080"
ENDPOINT_UPDATE = "/update"
ENDPOINT_FOLLOW = "/follow"

NUM_OF_RECORDS = 1000

# How many packets send in one batch
REQUEST_SIZE = 100

gen = Generator()

counter = 0
sent_packets = 0

used_ids = []
msg = []

while len(used_ids) <= NUM_OF_RECORDS:
    record = ""
    
    ID = random.randint(0, NUM_OF_RECORDS)
    if ID not in used_ids:
        # counter += 1
        used_ids.append(ID)

        record = gen.generate_node(ID)

        # Create a JSON from record
        fields = record.split(",")

        msg_dict = {
            "id": record.split(",")[0],
            "nickname": record.split(",")[1],
            "sex": record.split(",")[2],
            "nationality": record.split(",")[3]
        }
        msg.append(msg_dict)

        if len(msg) >= 100:
            sent_packets += REQUEST_SIZE

            print(f"Sending {sent_packets}/{NUM_OF_RECORDS} UPDATE packets")
            response = requests.put(f"{URL}{ENDPOINT_UPDATE}", json=msg)
            msg = []

print(30 * "=")

used_relations = []
msg = []

counter = 0
sent_packets = 0

while counter < NUM_OF_RECORDS:
    
    ID1 = random.randint(0, NUM_OF_RECORDS)
    # ID2 = random.randint(0, NUM_OF_RECORDS)

    NUM_OF_FOLLOWERS = random.randint(0, 10)

    for i in range(NUM_OF_FOLLOWERS):
        ID2 = random.randint(0, NUM_OF_RECORDS)

        if ID1 != ID2 \
            and [ID1, ID2] not in used_relations:

            used_relations.append([ID1, ID2])
            counter += 1

            record = gen.generate_relation(ID1, ID2, gen_timestamp=True)
            
            # Create a JSON from record
            fields = record.split(",")

            msg_dict = {
                "first": record.split(",")[0],
                "second": record.split(",")[1]
            }
            msg.append(msg_dict)

            if len(msg) >= REQUEST_SIZE:
                sent_packets += REQUEST_SIZE

                print(f"Sending {sent_packets}/{NUM_OF_RECORDS} FOLLOW packets")
                response = requests.put(f"{URL}{ENDPOINT_FOLLOW}", json=msg)
                msg = []

exit()