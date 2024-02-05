import requests

class _Pipelines:

    def __init__(self, client) -> None:
        self.client = client

    def one(self) -> requests.Response:
        return requests.get()