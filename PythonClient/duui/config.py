import dotenv


API_URL = "http://192.168.2.122:2605"  # api.duui.texttechnologylab.org

API_KEY = dotenv.dotenv_values(".env").get("API_KEY")
