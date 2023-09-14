import json
import requests


def load_pipeline(id: str) -> dict:
    response = requests.get(f"http://127.0.0.1:2605/pipelines/{id}")
    return response.json()


def load_pipelines() -> list[dict]:
    return requests.get("http://127.0.0.1:2605/pipelines").json()["pipelines"]


def update_pipeline(id: str, new_pipeline: dict) -> str:
    response = requests.put(f"http://127.0.0.1:2605/pipelines/{id}", data=json.dumps(new_pipeline))
    return response.text


def create_pipeline(name: str, components: list[dict]) -> str:
    pipeline = {"name": name, "components": components}
    response = requests.post("http://127.0.0.1:2605/pipelines", data=json.dumps(pipeline))
    return response.text