import requests

from duui.config import API_URL


class _Pipelines:
    def __init__(self, client: "DUUIClient") -> None:
        self._client = client


    def findOne(self, id: str, *, include_components: bool = True, include_statistics: bool = False) -> dict | None:
        """Retrieve a pipeline from the database using its id.

        Args:
            id (str): The id of the pipeline. Should be 24 character hex-string (MongoDB object id).
            include_components (bool, optional): Wether to include components in the pipeline object. Defaults to True.
            include_statistics (bool, optional): Wether to include statistics in the pipeline object. Defaults to False.

        Returns:
            dict | None: A pipeline object as a dictionary.
        """
        response = requests.get(
            f"{API_URL}/pipelines/{id}{self._client.to_query({
                "components": include_components,
                "statistics": include_statistics
            })}",
            headers=self._client._auth,
        )

        if response.ok:
            return response.json()
        return self._client.request_failed(response)
    
    def findMany(
        self,
        *,
        limit: int = 5,
        skip: int = 0,
        sort: str = "name",
        order: int = 1,
        include_components: bool = True,
        include_statistics: bool = False,
        include_templates: bool = False,
        include_count: bool = False
    ) -> dict | str:
        """Retrieve one or multiple pipelines by applying filters.

        Args:
            limit (int, optional): The maximum number of pipelines to return. Defaults to 5.
            skip (int, optional): The amount of pipelines to skip before a limit is applied. Defaults to 0.
            sort (str, optional): The criteria to sort by. Defaults to "name".
            order (int, optional): The order to sort by. Defaults to 1 (ascending).
            include_components (bool, optional): Wether to include components in the pipeline objects. Defaults to True.
            include_statistics (bool, optional): Wether to include statistics in the pipeline objects. Defaults to False.
            include_templates (bool, optional): Wether to include templates. Defaults to False.
            include_count (bool, optional): Wether to include the total count of pipelines before a limit is applied. Defaults to False.
        Returns:
            dict | str: A dictionary containing matching pipelines and optionally the total count.
        """

        data = {
            "limit": min(limit, 50),
            "skip": skip,
            "sort": sort,
            "order": order,
            "templates": include_templates,
            "components": include_components,
            "statistics": include_statistics
        }

        response = requests.get(
            f"{API_URL}/pipelines{self._client.to_query(data)}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        if not include_count:
            return response.json()["pipelines"]
        
        return response.json()

    def create(self, name: str, components: list[dict], *, description: str = "", tags: list[str] = None, settings: dict = None, insert_as_template: bool = False) -> dict | str:
        data = {
            "name": name,
            "components": components,
            "description": description,
            "tags": list(set(tags)) if tags is not None else [],
            "settings": settings if settings is not None else {},
            "template": insert_as_template,
        }
        
        response = requests.post(
            f"{API_URL}/pipelines",
            json=data,
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        return response.json()
    

    def updateOne(self, id: str, updates: dict) -> dict | str:
        """Updates a pipeline given its id and a dichtionary of updates to set.

        Args:
            id (str): The id of the pipeline. Should be 24 character hex-string (MongoDB object id).
            updates (dict): The updates to apply to the pipeline.

        Returns:
            dict | str: The fields that have been updated.
        """
        response = requests.put(
            f"{API_URL}/pipelines/{id}",
            json=updates,
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        return response.json()

    def delete(self, id: str) -> str:
        """Attempts to delete a pipeline given its id.

        Args:
            id (str): The id of the pipeline. Should be 24 character hex-string (MongoDB object id).

        Returns:
            str: A message telling the deletion status.
        """
        response = requests.delete(
            f"{API_URL}/pipelines/{id}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        return response.text

    def instantiate(self, id: str) -> dict | str:
        """Instantiate a pipeline given its id.

        Args:
            id (str): The id of the pipeline. Should be 24 character hex-string (MongoDB object id).

        Returns:
            dict | str: The state of the pipeline after instantiating.
        """
        response = requests.post(
            f"{API_URL}/pipelines/{id}/start",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        return response.json()

    def shutdown(self, id: str) -> dict | str:
        """Shut down a pipeline given its id. All active processes using the pipeline are cancelled.

        Args:
            id (str): The id of the pipeline. Should be 24 character hex-string (MongoDB object id).

        Returns:
            dict | str: The state of the pipeline after instantiating.
        """

        response = requests.put(
            f"{API_URL}/pipelines/{id}/stop",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)
        
        return response.json()
