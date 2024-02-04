import json

from dotenv import dotenv_values
from duui.client import DUUIClient


def main() -> None:
    config = dotenv_values(".env")
    API_KEY = config["API_KEY"]

    client = DUUIClient(API_KEY)
    pipeline = client.fetch_pipeline("653f8f749f717510ec2e9767")

    print(pipeline)


if __name__ == "__main__":
    main()
