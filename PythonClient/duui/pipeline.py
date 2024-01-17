from __future__ import annotations
from dataclasses import dataclass, field
from datetime import datetime
from enum import Enum, auto

import json
import requests


class Driver(Enum):
    UIMA = "DUUIUIMADriver"
    Remote = "DUUIRemoteDriver"
    Docker = "DUUIDockerDriver"
    Swarm = "DUUISwarmDriver"


@dataclass
class DUUIPipeline:
    oid: str = ""

    name: str
    description: str = ""
    tags: list[str] = field(default_factory=list)
    state: str

    createdAt: int = datetime.now().timestamp
    timesUsed: int = 0
    lastUsed: int | None = None

    settings: dict = field(default_factory=dict)
    components: list[DUUIComponent]

    user_id: str = ""


class DUUIComponent:
    def __init__(
        self,
        name: str,
        settings: DUUIComponentSettings,
        description: str = "",
        categories: list[str] = None,
    ) -> None:
        self.oid: str = ""
        self.id: str = ""
        self.name: str = name
        self.description: str = description
        self.categories: list[str] = categories or []
        self.status: str = "Unknown"
        self.createdAt: int = -1
        self.serviceStartTime: int = -1
        self.timesUsed: int = 0
        self.lastUsed: int = -1
        self.settings: DUUIComponentSettings = settings
        self.pipeline_id: str = ""
        self.user_id: str = ""


@dataclass
class DUUIComponentSettings:
    driver: Driver
    target: str
    options: dict = field(default_factory=dict)
    parameters: dict = field(default_factory=dict)


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


if __name__ == "__main__":
    settings = DUUIComponentSettings(Driver.Docker, "dohasdiosad")
    component = DUUIComponent("Python", settings, "dasioudasdiuzasdasd", ["A", "B"])
    pipeline = DUUIPipeline("Python", [component])
