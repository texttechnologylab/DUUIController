import json
from dotenv import dotenv_values

from client import DUUIClient


def main() -> None:
    config = dotenv_values(".env")
    API_KEY = config["API_KEY"]
    client = DUUIClient(API_KEY)
    response = client.pipelines().many(
        3, sort="name", order=-1, templates=False, components=False
    )
    print(json.dumps(response.json(), indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
