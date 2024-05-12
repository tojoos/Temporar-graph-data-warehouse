import random
import time

class Generator:
    def __init__(self):
        self.SEX = ["M", "F"]
        self.NATIONALITIES = ["PL", "DE", "CZ", "UA", "SK", "FR", "RO", "LV", "HU", "IT"]

    @staticmethod
    def __generate_nickname():
        adjectives = ['funny', 'clever', 'brave', 'wise', 'mysterious', 'playful', 'quirky', 'silly', 'friendly', 'curious']

        nouns = ['cat', 'dog', 'penguin', 'unicorn', 'dragon', 'elephant', 'monkey', 'fox', 'owl', 'tiger']

        adjective = random.choice(adjectives)
        noun = random.choice(nouns)
        number = random.randint(100, 999)
        return f'{adjective}_{noun}_{number}'
    
    def generate_node(self, ID: int) -> str:
        record = ""
        record += str(ID)
        record += ","

        record += self.__generate_nickname()
        record += ","

        record += str(self.SEX[random.randint(0, 1)])
        record += ","

        record += str(self.NATIONALITIES[random.randint(0, 9)])
        
        return record

    @staticmethod
    def generate_relation(ID1: int, ID2: int, gen_timestamp=False) -> str:

        record = f"{ID1},{ID2}"
        
        if gen_timestamp:
            timestamp = int(time.time()) - random.randint(0, 1000)
            record += f",{timestamp}"
        
        return record