import requests

SERVER_ADDRESS = "http://127.0.0.1"
SERVER_PORT = 8000

SERVER_ENDPOINT = "pdf/merge"

SERVER_URL = f"{SERVER_ADDRESS}:{SERVER_PORT}/{SERVER_ENDPOINT}"

if __name__ == '__main__':
    data = requests.get(SERVER_URL, files=[('files', open("A.pdf", "rb")), ('files', open("B.pdf", "rb"))]).content

    with open("out.pdf", "wb") as f:
        f.write(data)

    print(data)