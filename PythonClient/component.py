import json
from typing import Mapping
import requests


def load_component(id: str) -> dict:
    response = requests.get(f"http://192.168.2.122:2605/components/{id}")
    return response.json()


def load_components() -> list[dict]:
    return requests.get("http://192.168.2.122.122:2605/components").json()["components"]


def update_component(id: str, new_component: dict) -> str:
    response = requests.put(f"http://192.168.2.122:2605/components/{id}", data=json.dumps(new_component))
    return response.text


def create_component(
    name: str, categories: list[str], description: str, settings: Mapping, status: str = "None", pipeline_id: str = None, user_id: str = None
) -> dict:
    if settings["driver"] not in ("DUUIRemoteDriver", "DUUIDockerDriver", "DUUISwarmDriver", "DUUIUIMADriver"):
        return f"Invalid driver <{settings.driver}>."

    if name == "":
        return "Name cannot be empty"

    if not settings["driver"]:
        return "Driver cannot be empty"

    if not settings["target"]:
        return "Target cannot be empty"

    if pipeline_id is None and user_id is None:
        return "Either pipeline_id or user_id must be set"

    response = requests.post(
        "http://192.168.2.122:2605/components",
        data=json.dumps(
            {
                "name": name,
                "categories": categories,
                "settings": settings,
                "description": description,
                "status": "",
                "pipeline_id": pipeline_id,
                "user_id": user_id,
            }
        ),
        headers={"session": "3ba62df3-5022-4c1d-ae8b-3301e4a46b4e"},
    )
    return response.json()


def delete_component(id: str) -> dict:
    response = requests.delete(f"http://192.168.2.122:2605/components/{id}")
    return response.json()
