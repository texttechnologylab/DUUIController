from duui import DUUIClient
from pipeline import DUUIPipeline


def main() -> None:
    API_KEY = "vmHSSFXaW6xaHtf84um7XugNorNAM60PJRJkb3kAjtZ0Dz5NiA46BNRKgAycVsbdH+pqvuhIIF+OU02A3yvv0Q=="
    CLIENT = DUUIClient(API_KEY)

    CLIENT.Pipelines.update_one(
        "653f8f749f717510ec2e9767",
        {"name": "My Python Pipeline", "settings": {"timeout": 2000}},
    )


if __name__ == "__main__":
    main()
