from __future__ import annotations
import json

import requests


class DUUIClient:

    def __init__(self, key: str) -> None:
        self._url = "http://192.168.2.122:2605"
        self._auth = {"Authorization": key}

    def pipelines(self) -> Pipelines:
        return Pipelines(self)


class Pipelines:
    def __init__(self, client: DUUIClient) -> None:
        self.__client = client

    def one(self, id: str, components: bool = True) -> requests.Response:
        return requests.get(
            f"{self.__client._url}/pipelines/{id}",
            params=json.dumps({"components": components}),
            headers=self.__client._auth,
        )

    def many(
        self,
        limit: int = 10,
        skip: int = 0,
        sort: str = "name",
        order: int = 1,
        templates: bool = True,
        components: bool = True,
    ) -> requests.Response:
        return requests.get(
            f"{self.__client._url}/pipelines?limit={max(1, min(limit, 50))}&skip={max(0, skip)}&sort={sort}&order={order}&templates={templates}&components={components}",
            headers=self.__client._auth,
        )
