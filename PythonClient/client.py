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
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/textimager-duui-ddc-fasttext:latest",
            "description": "Detect the language of a document using the fast FastText model.",
            "options": {},
        },
        {
            "name": "spaCy",
            "category": "Annotation",
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/textimager-duui-spacy-single-de_core_news_sm:latest",
            "description": "Annotate a document using spaCy.",
            "options": {},
        },
        {
            "name": "BreakIteratorSegmenter",
            "category": "Tokenizer",
            "driver": "DUUIUIMADriver",
            "target": "de.tudarmstadt.ukp.dkpro.core.tokit.BreakIteratorSegmenter",
            "description": "Split the document into Tokens using the DKPro BreakIteratorSegmenter AnalysisEngine.",
            "options": {},
        },
        {
            "name": "HeidelTime Annotator",
            "category": "Time",
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/heideltime_ext:0.2",
            "description": "Extract temporal expressions from documents and normalize them according to the TIMEX3 annotation standard.",
            "options": {"useGPU": True, "withDockerImageFetching": True},
        },
        {
            "name": "GerVader",
            "category": "Sentiment",
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/gervader_duui:latest",
            "description": "GerVADER is a German adaptation of the sentiment classification tool VADER. Classify sentences into positive, negative or neutral statements.",
            "options": {},
        },
        {
            "name": "GNFinder",
            "category": "Names",
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/gnfinder:latest",
            "description": "Very fast finder of scientific names. It uses dictionary and NLP approaches.",
            "options": {"useGPU": True, "withDockerImageFetching": True},
        },
        {
            "name": "SentimentBERT (German)",
            "category": "Sentiment",
            "driver": "DUUIDockerDriver",
            "target": "docker.texttechnologylab.org/german-sentiment-bert:latest",
            "description": "Pre-trained BERT model. Provides a better understanding of words and sentences in the context",
            "options": {"useGPU": True, "withDockerImageFetching": True},
        },
    ]

    # for component in components[1:2]:
    #     response = create_component(
    #         component["name"],
    #         component["category"],
    #         component["driver"],
    #         component["target"],
    #         component["description"],
    #         component["options"] if component["options"] else None,
    #     )

    id = "65030e56200b892ea7805da6"
    print(start_process(id, options={"document": "Das ist ein Testsatz vom 14. September 2023."}))
