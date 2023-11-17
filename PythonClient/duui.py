import requests
import secrets

class DUUI:
    api_key: str

    class Pipelines:
        @staticmethod
        def find_one(id: str) -> dict:
            response = requests.get(
                f"http://192.168.2.122:2605/pipelines/{id}",
                headers={"authorization": DUUI.api_key},
            )
            return response.json()

    def __init__(self, api_key: str) -> None:
        DUUI.api_key = api_key


if __name__ == "__main__":
    key = "mcadLUsG4nSeBIcFlQ3CbvM953sGlND0FEvRIDpZzy5ir1RtyncSHZaBhG/ur9ZqYrBcu56dvhDlmePM4rRuqQ=="
    print(DUUI(key).Pipelines.find_one("653f8f749f717510ec2e9767"))