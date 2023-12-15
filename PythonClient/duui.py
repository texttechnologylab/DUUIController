import json
import requests

from pipeline import DUUIComponent, DUUIComponentSettings, DUUIPipeline, Driver


def as_json(json_dict: dict) -> str:
    return json.dumps(json_dict, indent=2, ensure_ascii=False)


class DUUI:
    api_key: str

    class Pipelines:
        @staticmethod
        def find_one(id: str) -> dict:
            response = requests.get(
                f"http://192.168.2.122:2605/pipelines/{id}",
                headers={"authorization": DUUI.api_key},
            )
            return as_json(response.json())

        @staticmethod
        def find_many(limit: int = 0, skip: int = 0) -> dict:
            response = requests.get(
                f"http://192.168.2.122:2605/pipelines/user/all?limit={limit}&skip={skip}",
                headers={"authorization": DUUI.api_key},
            )
            return as_json(response.json()["pipelines"])

        @staticmethod
        def update_one(id: str, data: dict) -> str:
            response = requests.put(
                f"http://192.168.2.122:2605/pipelines/{id}",
                data=json.dumps(data),
                headers={"authorization": DUUI.api_key},
            )
            return as_json(response.json())

        @staticmethod
        def create(pipeline: DUUIPipeline) -> str:
            response = requests.post(
                f"http://192.168.2.122:2605/pipelines",
                data=json.dumps(
                    {
                        "name": pipeline.name,
                        "description": pipeline.description,
                        "components": pipeline.components,
                    }
                ),
                headers={"authorization": DUUI.api_key},
            )

            return as_json(response.json())

    def __init__(self, api_key: str) -> None:
        DUUI.api_key = api_key


if __name__ == "__main__":
    key = "vmHSSFXaW6xaHtf84um7XugNorNAM60PJRJkb3kAjtZ0Dz5NiA46BNRKgAycVsbdH+pqvuhIIF+OU02A3yvv0Q=="
    client = DUUI(key)

    pipeline = client.Pipelines.find_one("653f8f749f717510ec2e9767")
    print(pipeline)
    # oid = pipeline["oid"]

    # pipeline = client.Pipelines.update_one(
    #     oid,
    #     {
    #         "name": "Eine simple Pipeline mit nur einer Komponente",
    #         "description": "Das ist eine Beschreibung, die ich mit Python verschickt habe. Eigentlich dient diese Pipeline nur dafür Tests durchzuführen, da sie schnell ausführbar ist (auch für eine große Zahl an Dokumenten).",
    #     },
    # )
    # print(pipeline)
    # settings = DUUIComponentSettings(Driver.Docker, "dohasdiosad")
    # component = DUUIComponent("Python", settings, "dasioudasdiuzasdasd", ["A", "B"])
    # pipeline = DUUIPipeline("Python", [component])
    # print(client.Pipelines.create(pipeline))
