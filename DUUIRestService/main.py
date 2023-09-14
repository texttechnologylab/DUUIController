import datetime
import json
import time
import requests


def load_pipeline(id: str) -> dict:
    response = requests.get(f"http://127.0.0.1:2605/pipelines/{id}")
    return response.json()


def load_pipelines() -> list[dict]:
    return requests.get("http://127.0.0.1:2605/pipelines").json()["pipelines"]


def update_pipeline(id: str, new_pipeline: dict) -> str:
    response = requests.put(f"http://127.0.0.1:2605/pipelines/{id}", data=json.dumps(new_pipeline))
    return response.text


def create_pipeline(name: str, components: list[dict]) -> str:
    pipeline = {"name": name, "components": components}
    response = requests.post("http://127.0.0.1:2605/pipelines", data=json.dumps(pipeline))
    return response.text


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


if __name__ == "__main__":
    name = "MyPythonPipeline2"
    components = [
        {"name": "MyPythonComponent1", "driver": "DUUIDockerDriver", "target": "docker.texttechnologylab.org/textimager-duui-ddc-fasttext:latest"},
        {"name": "MyPythonComponent2", "driver": "DUUIUIMADriver", "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter"},
        {"name": "MyPythonComponent3", "driver": "DUUIDockerDriver", "target": "docker.texttechnologylab.org/heideltime_ext:0.2"},
        {"name": "MyPythonComponent4", "driver": "DUUIDockerDriver", "target": "docker.texttechnologylab.org/gervader_duui:latest"},
    ]

    create_pipeline(name, components)
    pipelines = load_pipelines()

    for pipeline in pipelines:
        start_process(pipeline_id=pipeline["id"])

    for pipeline in pipelines:
        processes = load_processes(pipeline["id"])
        for process in processes:
            print(json.dumps(process, indent=2))
            # print(get_status(process["id"]))
            # print(get_progress(process["id"]))
            # print(cancel_process(process["id"]))
            # print(json.dumps(process, indent=2))
