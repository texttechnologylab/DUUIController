import json
from process import start_process
from duui.pipeline import load_pipeline
from duui.pipeline import create_pipeline
from duui.pipeline import load_pipelines
from component import load_components, create_component, delete_component

if __name__ == "__main__":
    name = "MyPythonPipeline2"
    components = [
        {
            "name": "LanguageDetection FastText",
            "categories": ["Language"],
            "description": "Detect the language of a document using the fast FastText model.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/textimager-duui-ddc-fasttext:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "spaCy",
            "categories": ["Token", "Sentence", "Part of Speech", "Named Entity", "Lemma"],
            "description": "Annotate a document using spaCy.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "BreakIteratorSegmenter",
            "categories": ["Token", "Sentence"],
            "description": "Split the document into Tokens using the DKPro BreakIteratorSegmenter AnalysisEngine.",
            "settings": {
                "driver": "DUUIUIMADriver",
                "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
                "options": {},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "HeidelTime Annotator",
            "categories": ["Time"],
            "description": "Extract temporal expressions from documents and normalize them according to the TIMEX3 annotation standard.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/heideltime_ext:0.2",
                "options": {"useGPU": True, "withDockerImageFetching": True},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "GerVader",
            "categories": ["Sentiment"],
            "description": "GerVADER is a German adaptation of the sentiment classification tool VADER. Classify sentences into positive, negative or neutral statements.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/gervader_duui:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "GNFinder",
            "categories": ["Named Entity"],
            "description": "Very fast finder of scientific names. It uses dictionary and NLP approaches.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/gnfinder:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
                "parameters": {},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
        {
            "name": "SentimentBERT (German)",
            "categories": ["Sentiment"],
            "description": "Pre-trained BERT model. Provides a better understanding of words and sentences in the context",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/german-sentiment-bert:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
            },
            "user_id": "65058bbe461285048191ef9a",
            "pipeline_id": None,
            "status": "None",
        },
    ]

    for component in components:
        print(
            create_component(
                component["name"],
                component["categories"],
                component["description"],
                component["settings"],
                component["status"],
                component["pipeline_id"],
                component["user_id"],
            )
        )

    # import requests

    # response = requests.get(
    #     "http://192.168.2.122:2605/pipelines/6510683fef55580233da1f79", headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"}
    # )

    # pipeline = response.json()
    # pipeline_id = pipeline["id"]

    # # response = requests.post(
    # #     "http://192.168.2.122:2605/processes",
    # #     headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"},
    # #     data=json.dumps(
    # #         {
    # #             "pipeline_id": pipeline_id,
    # #             "input": {"source": "Dropbox", "path": "/sample_txt", "extension": ".txt"},
    # #             "output": {"type": "Dropbox", "path": "/output/txt"},
    # #         }
    # #     ),
    # # # )

    # # response = requests.post(
    # #     "http://192.168.2.122:2605/processes",
    # #     headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"},
    # #     data=json.dumps(
    # #         {
    # #             "pipeline_id": pipeline_id,
    # #             "input": {"source": "Dropbox", "path": "/sample_gz", "extension": ".gz"},
    # #             "output": {"type": "Dropbox", "path": "/output/gz"},
    # #         }
    # #     ),
    # # )

    # response = requests.post(
    #     "http://192.168.2.122:2605/processes",
    #     headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"},
    #     data=json.dumps(
    #         {
    #             "pipeline_id": pipeline_id,
    #             "input": {"source": "Dropbox", "path": "/Bundestag", "extension": ".txt"},
    #             "output": {"type": "Minio", "path": "bundestag"},
    #         }
    #     ),
    # )

    # response = requests.post(
    #     "http://192.168.2.122:2605/processes",
    #     headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"},
    #     data=json.dumps(
    #         {
    #             "pipeline_id": pipeline_id,
    #             "input": {"source": "Dropbox", "path": "/sample_xmi", "extension": ".xmi"},
    #             "output": {"type": "Minio", "path": "xmi"},
    #         }
    #     ),
    # )

    # response = requests.post(
    #     "http://192.168.2.122:2605/processes",
    #     headers={"session": "18866d1a-6afe-46c5-b236-5d17115da6ce"},
    #     data=json.dumps(
    #         {
    #             "pipeline_id": pipeline_id,
    #             "input": {"source": "Dropbox", "path": "/sample_txt", "extension": ".txt"},
    #             "output": {"type": "Minio", "path": "txt"},
    #         }
    #     ),
    # )

    # f0 = open("PythonClient/component.py", 'rb')
    # f1 = open("PythonClient/pipeline.py", 'rb')

    # response = requests.post("http://192.168.2.122:2605/files", files={"component.py": f0, "pipeline.py": f1})

    # f0.close()
    # f1.close()

    # print(response.text)

    # id = "65030e56200b892ea7805da6"
    # print(start_process(id, options={"document": "Das ist ein Testsatz vom 14. September 2023."}))
