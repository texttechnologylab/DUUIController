import requests

from duui.config import API_URL


class _Components:

    def __init__(self, client: "DUUIClient") -> None:
        self._client = client