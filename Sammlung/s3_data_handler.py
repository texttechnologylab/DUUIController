import os
import boto3
import pathlib

access_key = "A"
secret_access_key = "B"


S3Client = "S3.Client"

def get_s3_client(access_key: str, secret_access_key: str) -> S3Client:
    return boto3.client("s3", aws_access_key_id=access_key, aws_secret_access_key=secret_access_key)

if __name__ == "__main__":
    client = get_s3_client(access_key, secret_access_key)


    bucket = "test-bucket"
    for file in os.listdir():
        if os.path.isdir(file):
            continue
        client.upload_file(file, bucket, str(file))
