from __future__ import annotations
import json


class DUUIComponent:
    def __init__(self, data: dict) -> None:
        self.__data = {}

        for key, value in data.items():
            if key not in [
                "oid",
                "name",
                "description",
                "status",
                "categories",
                "created_at",
                "modified_at",
                "settings",
                "index",
                "pipeline_id",
                "user_id",
            ]:
                continue

            self.__data[key] = value

    def __str__(self) -> str:
        return json.dumps(self.__data, indent=2, ensure_ascii=False)

    def __repr__(self) -> str:
        return f"DUUIComponent(oid={self.oid}, modified_at={self.modified_at if self.modified_at > 0 else 'Never'})"

    @property
    def oid(self) -> str:
        return self.__data.get("oid", "")

    @property
    def name(self) -> str:
        return self.__data.get("name", "")

    @property
    def description(self) -> str:
        return self.__data.get("description", "")

    @property
    def categories(self) -> list[str]:
        return self.__data.get("categories", [])

    @property
    def status(self) -> str:
        return self.__data.get("status", "")

    @property
    def created_at(self) -> int:
        return self.__data.get("created_at", -1)

    @property
    def modified_at(self) -> int:
        return self.__data.get("modified_at", -1)

    @property
    def driver(self) -> str:
        return self.settings.get("driver", "")

    @property
    def target(self) -> str:
        return self.settings.get("target", "")

    @property
    def index(self) -> int:
        return self.__data.get("index", -1)

    @property
    def settings(self) -> dict:
        return self.__data.get("settings", {})

    @property
    def options(self) -> dict:
        return self.settings.get("options", {})

    @property
    def parameters(self) -> dict:
        return self.settings.get("parameters", {})

    @property
    def pipeline_id(self) -> str:
        return self.__data.get("pipeline_id", "")

    @property
    def user_id(self) -> str:
        return self.__data.get("user_id", "")
