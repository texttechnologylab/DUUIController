import json
from typing import Mapping
import requests


def load_component(id: str) -> dict:
    response = requests.get(f"http://127.0.0.1:2605/components/{id}")
    return response.json()


def load_components() -> list[dict]:
    return requests.get("http://127.0.0.1:2605/components").json()["components"]


def update_component(id: str, new_component: dict) -> str:
    response = requests.put(f"http://127.0.0.1:2605/components/{id}", data=json.dumps(new_component))
    return response.text


def create_component(name: str, category: str, driver: str, target: str, description: str, options: Mapping = None) -> dict:
    if driver not in ("DUUIRemoteDriver", "DUUIDockerDriver", "DUUISwarmDriver", "DUUIUIMADriver"):
        return f"Invalid driver <{driver}>."

    if name == "":
        return "Name cannot be empty."
    if category == "":
        return "Category cannot be empty."
    if target == "":
        return "Target cannot be empty."

    response = requests.post(
        "http://127.0.0.1:2605/components",
        data=json.dumps({"name": name, "category": category, "driver": driver, "target": target, "description": description, "options": options}),
    )
    return response.json()


def delete_component(id: str) -> dict:
    response = requests.delete(f"http://127.0.0.1:2605/components/{id}")
    return response.json()
