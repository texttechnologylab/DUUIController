## Python Client

This simple Python client for DUUI handles the creation and management of pipelines and processes. Raw requests are hidden behind functions that are called instead.

## Usage

To use the client create a `.env` file in the PythonClient folder and provide an API key from the web interface as well as the API URL (https://api.duui.texttechnologylab.org/).

A Client instance can be created like this:

```
from duui.client import DUUIClient
from duui.config import API_KEY

CLIENT = DUUIClient(API_KEY)
```

To retrieve one or more pipelines use:

```
CLIENT.pipelines.findMany(limit=5)
```

## Note

This Client is incomplete and not optimized. It's sole purpose is to highlight the benefit of having a Client to handle api requests with Python.

