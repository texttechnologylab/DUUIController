import requests

from user import DUUIUser


class DUUIClient:
    def __init__(self, api_key: str) -> None:
        self.api_key = api_key
        self.user = self.__validate_api_key()
        if not self.user:
            raise ValueError("Invalid api key")

    def __validate_api_key(self) -> bool:
        user = self.__fetch_user_by_api_key()
        return user

    def __fetch_user_by_api_key(self) -> DUUIUser:
        return requests.get(f"http://192.168.2.122:2605/users/auth/{self.api_key}").json()

    def fetch_pipeline(self, pipeline_id: str) -> dict:
        return requests.get(f"http://192.168.2.122:2605/pipelines/{pipeline_id}", headers={"session": self.user["session"]}).json()

    def fetch_pipelines(self) -> dict:
        return requests.get(f"http://192.168.2.122:2605/pipelines/all/{self.user['id']}").json()


if __name__ == "__main__":
    client = DUUIClient("6e150c69-c6f2-403b-968a-4ba274adadb7")
    print(client.fetch_pipeline("650d81296569b14506ce29ba")['components'][0]["description"])
