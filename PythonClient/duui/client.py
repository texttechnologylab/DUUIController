import json
import requests
from duui.objects.components import _Components

from duui.objects.pipelines import _Pipelines
from duui.objects.processes import _Processes


class DUUIClient:

    def __init__(self, key: str) -> None:
        self._auth = {"Authorization": key}

        self._pipelines = _Pipelines(self)
        self._processes = _Processes(self)
        self._components = _Components(self)

    def request_failed(self, response: requests.Response) -> str:
        return f"Request failed with status code {response.status_code} and message {response.text}"

    @staticmethod
    def to_query(parameters: dict) -> str:
        return "?" + "&".join(
            f"{key}={json.dumps(value) if isinstance(value, bool) else value}"
            for key, value in parameters.items()
        )

    @property
    def pipelines(self) -> _Pipelines:
        return self._pipelines

    @property
    def processes(self) -> _Processes:
        return self._processes

    @property
    def components(self) -> _Components:
        return self._components
