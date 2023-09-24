import json
from process import start_process
from pipeline import load_pipeline
from pipeline import create_pipeline
from pipeline import load_pipelines
from component import load_components, create_component, delete_component

if __name__ == "__main__":
    name = "MyPythonPipeline2"
    components = [
        {
            "name": "LanguageDetection FastText",
            "category": "Language",
            "description": "Detect the language of a document using the fast FastText model.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/textimager-duui-ddc-fasttext:latest",
                "options": {},
            },
        },
        {
            "name": "spaCy",
            "category": "Annotation",
            "description": "Annotate a document using spaCy.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest",
                "options": {},
            },
        },
        {
            "name": "BreakIteratorSegmenter",
            "category": "Tokenizer",
            "description": "Split the document into Tokens using the DKPro BreakIteratorSegmenter AnalysisEngine.",
            "settings": {
                "driver": "DUUIUIMADriver",
                "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
                "options": {},
            },
        },
        {
            "name": "HeidelTime Annotator",
            "category": "Time",
            "description": "Extract temporal expressions from documents and normalize them according to the TIMEX3 annotation standard.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/heideltime_ext:0.2",
                "options": {"useGPU": True, "withDockerImageFetching": True},
            },
        },
        {
            "name": "GerVader",
            "category": "Sentiment",
            "description": "GerVADER is a German adaptation of the sentiment classification tool VADER. Classify sentences into positive, negative or neutral statements.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/gervader_duui:latest",
                "options": {},
            },
        },
        {
            "name": "GNFinder",
            "category": "Names",
            "description": "Very fast finder of scientific names. It uses dictionary and NLP approaches.",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/gnfinder:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
            },
        },
        {
            "name": "SentimentBERT (German)",
            "category": "Sentiment",
            "description": "Pre-trained BERT model. Provides a better understanding of words and sentences in the context",
            "settings": {
                "driver": "DUUIDockerDriver",
                "target": "docker.texttechnologylab.org/german-sentiment-bert:latest",
                "options": {"useGPU": True, "withDockerImageFetching": True},
            },
        },
    ]

    # for component in components:
    #     response = create_component(
    #         component["name"],
    #         component["category"],
    #         component["description"],
    #         component["settings"],
    #     )

    import requests

    f0 = open("PythonClient/component.py", 'rb')
    f1 = open("PythonClient/pipeline.py", 'rb')

    response = requests.post("http://127.0.0.1:2605/files", files={"component.py": f0, "pipeline.py": f1})

    f0.close()
    f1.close()

    print(response.text)

    # id = "65030e56200b892ea7805da6"
    # print(start_process(id, options={"document": "Das ist ein Testsatz vom 14. September 2023."}))
