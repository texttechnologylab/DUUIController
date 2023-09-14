import json
import requests


def start_process(pipeline_id: str, options: dict = None) -> str:
    if options is None:
        options = dict()

    response = requests.post("http://127.0.0.1:2605/processes", data=json.dumps({"pipeline_id": pipeline_id, "options": options}))
    return response.text


def cancel_process(id: str) -> str:
    response = requests.put(f"http://127.0.0.1:2605/processes/{id}")
    return response.text


def load_processe(id: str) -> dict:
    return requests.get(f"http://127.0.0.1:2605/processes/{id}").json()


def load_processes(pipeline_id: str) -> dict:
    return requests.get(f"http://127.0.0.1:2605/pipelines/{pipeline_id}/processes").json()["processes"]


def get_status(id: str) -> str:
    return requests.get(f"http://127.0.0.1:2605/processes/{id}/status").json()


def get_progress(id: str) -> str:
    return requests.get(f"http://127.0.0.1:2605/processes/{id}/progress").json()
