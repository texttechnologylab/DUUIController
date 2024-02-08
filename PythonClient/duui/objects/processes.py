import json
import requests

from duui.config import API_URL


class _Processes:

    def __init__(self, client: "DUUIClient") -> None:
        self._client = client

    def findOne(self, id: str) -> dict | None:
        """Retrieve one process by its id.

        Args:
            id (str): The id of the process. Should be 24 character hex-string (MongoDB object id).

        Returns:
            dict | None: The process as a dictionary.
        """
        response = requests.get(
            f"{API_URL}/processes/{id}",
            headers=self._client._auth,
        )

        if response.ok:
            return response.json()

        return self._client.request_failed(response)

    def findMany(
        self,
        pipeline_id: str,
        *,
        limit: int = 5,
        skip: int = 0,
        sort: str = "started_at",
        order: int = -1,
        status_filter: list[str] = None,
        input_filter: list[str] = None,
        output_filter: list[str] = None,
        include_count: bool = False,
    ) -> dict | str:
        """Retrieve one or multiple processes by applying filters.

        Args:
            pipeline_id (str): The id of the pipeline the processes belongs to. Should be 24 character hex-string (MongoDB object id).
            limit (int, optional): The maximum number of processes to return. Defaults to 5.
            skip (int, optional): The amount of processes to skip before a limit is applied. Defaults to 0.
            sort (str, optional): The criteria to sort by. Defaults to "started_at".
            order (int, optional): The order to sort by. Defaults to 1 (ascending).
            status_filter (list[str], optional): A list of status names. Defaults to None.
            input_filter (list[str], optional): A list of input providers. Defaults to None.
            output_filter (list[str], optional): A list of output providers. Defaults to None.
            include_count (bool, optional): Wether to include the total count of processes before a limit is applied. Defaults to False.

        Returns:
            dict | str: A dictionary containing matching processes and optionally the total count.
        """

        if status_filter is None:
            status_filter = ["Any"]

        if input_filter is None:
            input_filter = ["Any"]

        if output_filter is None:
            output_filter = ["Any"]

        data = {
            "pipeline_id": pipeline_id,
            "limit": min(limit, 50),
            "skip": skip,
            "sort": sort,
            "order": order,
            "status": ";".join(status_filter),
            "input": ";".join(input_filter),
            "output": ";".join(output_filter),
        }

        response = requests.get(
            f"{API_URL}/processes{self._client.to_query(data)}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        if not include_count:
            return response.json()["processes"]

        return response.json()

    def documents(
        self,
        process_id: str,
        *,
        limit: int = 5,
        skip: int = 0,
        sort: str = "started_at",
        order: int = -1,
        search: str = "",
        status_filter: list[str] = None,
        include_count: bool = False,
    ) -> dict | str:
        """Retrieve one or multiple documents by applying filters.

        Args:
            process_id (str): The id of the process the document belongs to. Should be 24 character hex-string (MongoDB object id).
            limit (int, optional): The maximum number of processes to return. Defaults to 5.
            skip (int, optional): The amount of processes to skip before a limit is applied. Defaults to 0.
            sort (str, optional): The criteria to sort by. Defaults to "started_at".
            order (int, optional): The order to sort by. Defaults to 1 (ascending).
            search (str, optional): A search query applied to documents.
            status_filter (list[str], optional): A list of status names. Defaults to None.
            include_count (bool, optional): Wether to include the total count of processes before a limit is applied. Defaults to False.

        Returns:
            dict | str: A dictionary containing matching documents and optionally the total count.
        """
        if status_filter is None:
            status_filter = ["Any"]

        data = {
            "limit": min(limit, 50),
            "skip": skip,
            "sort": sort,
            "order": order,
            "search": search,
            "status": ";".join(status_filter),
        }

        response = requests.get(
            f"{API_URL}/processes/{process_id}/documents{self._client.to_query(data)}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        if not include_count:
            return response.json()["processes"]

        return response.json()

    def events(self, process_id: str) -> dict | str:
        """Retrieve one or multiple events by applying filters.

        Args:
            process_id (str): The id of the process the document belongs to. Should be 24 character hex-string (MongoDB object id).

        Returns:
            dict | str: A dictionary containing matching events.
        """
        response = requests.get(
            f"{API_URL}/processes/{process_id}/events",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        return response.json()["timeline"]

    def delete(self, id: str) -> dict | str:
        """Attempts to delete a process given its id.

        Args:
            id (str): The id of the process. Should be 24 character hex-string (MongoDB object id).

        Returns:
            str: A message telling the deletion status.
        """

        response = requests.delete(
            f"{API_URL}/processes/{id}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        return response.text

    def start(
        self,
        pipeline_id: str,
        input: dict,
        *,
        output: dict = None,
        notify: bool = False,
        check_target: bool = False,
        recursive: bool = False,
        overwrite: bool = False,
        sort_by_size: bool = False,
        minimum_size: int = 0,
        worker_count: int = 1,
        ignore_errors: bool = True,
    ) -> dict | str:
        """Start a new process with the specified pipeline.

        Args:
            pipeline_id (str): The id of the pipeline the process should use for execution.
              Should be 24 character hex-string (MongoDB object id).

            input (dict): Settings for the input source.

            output (dict, optional): Settings for the output location. Defaults to None.

            notify (bool, optional): Notify on errors and completion. Defaults to False.

            check_target (bool, optional): Check the target location for documents that
              are present already (and should be ignored in the process). Defaults to False.

            recursive (bool, optional): Look for files recursivley in the input location. Defaults to False.
            overwrite (bool, optional): Overwrite existing files in the output location. Defaults to False.
            sort_by_size (bool, optional): Sort files in ascending order by size. Defaults to False.
            minimum_size (int, optional): Files smaller than this size in bytes are ignored. Defaults to 0.
            worker_count (int, optional): The number of threads to use. Defaults to 1.
            ignore_errors (bool, optional): Ignore errors for documents and skip to the next one. Defaults to True.

        Returns:
            dict | str: The process that has been started.
        """

        data = {
            "pipeline_id": pipeline_id,
            "input": input,
            "output": output if output is not None else {"provider": "None"},
            "settings": {
                "notify": notify,
                "check_target": check_target,
                "recursive": recursive,
                "overwrite": overwrite,
                "sort_by_size": sort_by_size,
                "minimum_size": minimum_size,
                "worker_count": worker_count,
                "ignore_errors": ignore_errors,
            },
        }

        response = requests.post(
            f"{API_URL}/processes",
            json=data,
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        return response.json()

    def cancel(self, id: str) -> dict | str:
        """Cancel a process given its id.

        Args:
            id (str): The id of the process. Should be 24 character hex-string (MongoDB object id).

        Returns:
            str: A message telling the cancel status.
        """
        response = requests.put(
            f"{API_URL}/processes/{id}",
            headers=self._client._auth,
        )

        if not response.ok:
            return self._client.request_failed(response)

        return response.text
