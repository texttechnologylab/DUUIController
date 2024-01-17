import json

import requests
from dotenv import dotenv_values
from client import DUUIClient


def main() -> None:
    config = dotenv_values(".env")
    API_KEY = config["API_KEY"]

    client = DUUIClient(API_KEY)
    response = client.fetch_pipeline("65a160d07108cc79fdb836fa")

    print(json.dumps(response, indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
