import json
from duui.client import DUUIClient
from duui.config import API_KEY

ID = "65c13d6a4497a87bd8442cb5"
CLIENT = DUUIClient(API_KEY)


def start_process() -> str:
    process = CLIENT.processes.start(
        ID,
        input={"provider": "Dropbox", "path": "/input", "file_extension": ".txt"},
        output={
            "provider": "Dropbox",
            "path": "/output/python",
            "file_extension": ".txt",
        },
        recursive=True,
        minimum_size=500,
        sort_by_size=True,
        worker_count=4,
    )
    print(json.dumps(process, indent=2))
    return process["oid"]


def find_processes() -> None:
    processes = CLIENT.processes.findMany(ID, limit=2, status_filter=["Failed"])
    print(json.dumps(processes, indent=2))


def instantiate_pipeline() -> None:
    print(CLIENT.pipelines.instantiate(ID))


def shutdown_pipeline() -> None:
    print(CLIENT.pipelines.shutdown(ID))


def update_pipeline() -> None:
    my_pipeline = CLIENT.pipelines.updateOne(
        id,
        {
            "tags": ["Python", "Sentence", "Sentiment"],
            "settings": {"timeout": 10000, "ignore_errors": True, "max_retries": 5},
        },
    )

    print(json.dumps(my_pipeline, indent=2))


def create_pipeline() -> None:
    my_pipeline = CLIENT.pipelines.create(
        name="Pipeline from python binding",
        components=[
            {
                "name": "Tokenizer",
                "tags": ["Token", "Sentence"],
                "description": """Split the document into Tokens and Sentences
                  using the DKPro BreakIteratorSegmenter AnalysisEngine.""",
                "driver": "DUUIUIMADriver",
                "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
            },
            {
                "name": "GerVADER",
                "description": """GerVADER is a German adaptation of the sentiment
                  classification tool VADER. Classify sentences into positive, negative or neutral statements.""",
                "tags": ["Sentiment", "German"],
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/gervader_duui:latest",
                "options": {"scale": 2, "use_GPU": True},
            },
        ],
        description="This pipeline has been created using the API with Python. It splits the document text into Tokens and Sentences and then analyzes the Sentiment of these Sentences.",
        tags=["Python", "Sentence", "Sentiment"],
    )

    print(json.dumps(my_pipeline, indent=2))


def find_pipeline() -> None:
    my_pipeline = CLIENT.pipelines.findOne(
        "653f8f749f717510ec2e9767", include_components=False, include_statistics=True
    )

    print(json.dumps(my_pipeline, indent=2))


def find_pipelines() -> None:
    two_pipelines = CLIENT.pipelines.findMany(limit=2, include_components=False)
    print(json.dumps(two_pipelines, indent=2))


def delete_pipeline() -> None:
    print(CLIENT.pipelines.delete(ID))


def fetch_status(process_id: str) -> None:
    import schedule
    import sys
    import time

    def update() -> None:
        process = CLIENT.processes.findOne(process_id)
        result = CLIENT.processes.documents(
            process_id, status_filter=["Failed"], include_count=True
        )
        total = len(process["document_names"])

        print(
            f"""Progress: {round(process['progress'] / total * 100)}%\t{result['count']} Documents have failed.\r""",
            end="",
        )

        if process["status"] in ["Completed", "Failed", "Cancelled"]:
            print(
                f"""Progress: {round(process['progress'] / total * 100)}%\t{result['count']} Documents have failed.""",
            )

            print(f"Process finished with status {process['status']}.")
            sys.exit(0)

    schedule.every(5).seconds.do(update)

    while True:
        schedule.run_pending()
        time.sleep(1)


def main() -> None:
    instantiate_pipeline()
    oid = start_process()
    fetch_status(oid)


if __name__ == "__main__":
    main()
