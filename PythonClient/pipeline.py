import json
import requests


def load_pipeline(id: str) -> dict:
    response = requests.get(f"http://192.168.2.122:2605/pipelines/{id}")
    return response.json()


def load_pipelines() -> list[dict]:
    response = requests.get("http://192.168.2.122:2605/pipelines")
    return response.json()


def update_pipeline(id: str, new_pipeline: dict) -> str:
    response = requests.put(
        f"http://192.168.2.122:2605/pipelines/{id}", data=json.dumps(new_pipeline)
    )
    return response.text


def create_pipeline(name: str, components: list[dict]) -> str:
    pipeline = {"name": name, "components": components}
    response = requests.post(
        "http://192.168.2.122:2605/pipelines", data=json.dumps(pipeline)
    )
    return response.text
