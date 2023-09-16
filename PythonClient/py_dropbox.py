from pathlib import Path
import dropbox


def upload_file(path: Path) -> None:
    with open(path, "rb") as uf:
        content = uf.read()
        box.files_upload(content, f"/{path.name}")


def load_file(dropbox_file: str) -> None:
    box.files_download_to_file("/document.txt", f"/{dropbox_file}")


if __name__ == "__main__":
    with open("token.txt", "r") as f:
        TOKEN = f.read()

    box = dropbox.Dropbox(TOKEN)
    # upload_file(Path("document.txt"))
    load_file("document.txt")
