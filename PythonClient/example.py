import sys
import time
import requests
import dotenv
import schedule

API_KEY = dotenv.dotenv_values(".env").get("API_KEY")
API_URL = "http://192.168.2.122:2605"
# api.duui.texttechnologylab.org


def create_pipeline() -> requests.Response:
    return requests.post(
        f"{API_URL}/pipelines",
        headers={"Authorization": API_KEY},
        json={
            "name": "Pipeline made with Python",
            "description": "This pipeline has been created using the API with Python. It splits the document text into Tokens and Sentences and then analyzes the Sentiment of these Sentences.",
            "tags": ["Python", "Sentence", "Sentiment"],
            "components": [
                {
                    "name": "Tokenizer",
                    "tags": ["Token", "Sentence"],
                    "description": "Split the document into Tokens and Sentences using the DKPro BreakIteratorSegmenter AnalysisEngine.",
                    "driver": "DUUIUIMADriver",
                    "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
                },
                {
                    "name": "GerVADER",
                    "description": "GerVADER is a German adaptation of the sentiment classification tool VADER. Classify sentences into positive, negative or neutral statements.",
                    "tags": ["Sentiment", "German"],
                    "driver": "DUUIDockerDriver",
                    "target": "docker.texttechnologylab.org/gervader_duui:latest",
                    "options": {"scale": 2, "use_GPU": True},
                },
            ],
        },
    )


def instantiate_pipeline(pipeline_id: str) -> requests.Response:
    print("Instantiating pipeline.")

    return requests.post(
        f"{API_URL}/pipelines/{pipeline_id}/start",
        headers={"Authorization": API_KEY},
    )


def shutdown_pipeline(pipeline_id: str) -> requests.Response:
    print("Shutting down pipeline.")

    return requests.put(
        f"{API_URL}/pipelines/{pipeline_id}/stop",
        headers={"Authorization": API_KEY},
    )


def run_process(pipeline_id: str) -> requests.Response:
    print(f"Running pipeline with id {pipeline_id}.")

    return requests.post(
        f"{API_URL}/processes",
        headers={"Authorization": API_KEY},
        json={
            "pipeline_id": pipeline_id,
            "input": {
                "provider": "Dropbox",
                "path": "/input",
                "file_extension": ".txt",
            },
            "output": {
                "provider": "Dropbox",
                "file_extension": ".xmi",
                "path": "/output/python-2",
            },
            "settings": {
                "recursive": True,
                "minimum_size": 0,
                "worker_count": 10,
                "check_target": True,
            },
        },
    )


def get_process_status(process_id: str) -> None:
    response = requests.get(
        f"{API_URL}/processes/{process_id}",
        headers={"Authorization": API_KEY},
    )

    if response.ok:
        process = response.json()
        print(
            f"Progress: {round(process['progress'] / len(process["document_names"]) * 100)}%\r", end=""
        )

        if response.json()["status"] == "Completed":
            print(
                f"Progress: {round(process['progress'] / len(process["document_names"]) * 100)}%"
            )
            print("Process finished. Exiting.")
            if process["output"]["provider"] != "None":
                print("Output has been written to " + process["output"]["path"])

            sys.exit(0)

    else:
        print(response.text)
        sys.exit(1)


def main() -> None:
    # response = create_pipeline()
    # if not response.ok:
    #     print("Pipeline create failed. Exiting.")
    #     return

    # pipeline = response.json()
    pipeline_id = "653f8f749f717510ec2e9767"
    # print(f"Pipeline with id {pipeline_id} has been created.")

    # response = instantiate_pipeline(pipeline_id)
    # if not response.ok:
    #     print("Failed to instantiate pipeline. Exiting.")
    #     return

    # print(f"Pipeline with id {pipeline_id} has been instantiated and is ready to use.")

    response = run_process(pipeline_id)
    if not response.ok:
        shutdown_pipeline(pipeline_id)
        print("Failed to instantiate pipeline. Shutting down.")
        return

    process = response.json()
    process_id = process["oid"]
    schedule.every(1).seconds.do(get_process_status, process_id)

    while True:
        schedule.run_pending()
        time.sleep(1)


if __name__ == "__main__":
    main()
