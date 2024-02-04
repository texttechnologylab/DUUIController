import datetime
import requests
from duui.objects.pipeline import DUUIPipeline


class DUUIClient:
    __URL = "http://192.168.2.122:2605"

    def __init__(self, key: str) -> None:
        self.__KEY = key

    def fetch_pipeline(self, pipeline_id: str) -> DUUIPipeline | str:
        response = requests.get(
            f"{DUUIClient.__URL}/pipelines/{pipeline_id}",
            headers={"Authorization": self.__KEY},
        )

        if response.ok:
            return DUUIPipeline(response.json())

        return response.text

    def fetch_pipelines(self, limit: int = 10, skip: int = 0) -> dict:
        try:
            response: requests.Response = requests.get(
                f"{DUUIClient.__URL}/pipelines?limit={limit}&skip={skip}",
                headers={"Authorization": self.__KEY},
            )
        except requests.exceptions.ConnectionError as exception:
            return {"error": "The connection to the API could not be established."}

        if response.ok:
            return response.json()

        return {"error": response.text}


if __name__ == "__main__":
    client = DUUIClient("6e150c69-c6f2-403b-968a-4ba274adadb7")
    # print(datetime.datetime.now().timestamp() * 1000)
    # print(datetime.datetime.fromtimestamp(1417601730000 / 1000))
    # print(client.fetch_pipeline("650d81296569b14506ce29ba")['components'][0]["description"])
