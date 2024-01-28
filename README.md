# DUUIController

### 1. Metrics

DUUI collects metrics at document level and stores these in DUUIDocument objects for later storage in a
database. Metrics include:

- Status
- Wait Duration
- Decode Duration
- Deserialize Duration
- Process Duration
- Document size in bytes
- Progress in the pipeline
- Errors
- Document count

These are presented in the Frontend or, if DebugLevel is set to Debug, in the console for a detailed overview.

### 2. Monitoring and Management

Since DUUI does not have a Graphical User Interface (GUI) by default, a web-based interface has been created
to monitor and maintain NLP tools in the browser. This offers an alternative to Java based NLP, that is not
accessible for inexperienced users.
In the web-interface first and foremost pipelines can be created by setting a name, a description (optional)
and settings that influence the pipeline's behavior at run time. Because a pipeline itself doesn't provide any
actual annotations, Analysis Engines (or components for DUUI) can also be created and maintained for each
created pipeline.

As components are the actual processors in a pipeline, more fine-grained settings are provided. These settings
are called options, a set of predefined enhancements like the option to use the GPU or replicate components
scaling. The other kind of settings are called parameters and offer a way for the user to set additional flags
or settings relevant to a specific Analysis Engine implementation, that are not known to DUUI in advance.
Parameters are simply a set of key value pairs that are passed to the AE.

Both components and pipelines can be created from templates, that are either provided by DUUI itself or are
created by the user and reused. This enhances reusibility because all the relevant settings are copied when a
tool is copied. Once a pipeline has been created, it can be used to start a so-called process or workflow that
analysis documents from a given input source and possibly writes the output to a target cloud provider.

### 3. Backend

To reflect database CRUD operations, those methods have beend mapped to routes or endpoints that can be
reached by users. To manipulate resources in the database the user is asked to provide an authorization method
like an API key, that has to be generated in the web-interface. All endpoints communicate via REST and
transfer data in the json format for consistency and ease of conversion for the use with MongoDB. There are
three path groups that are accesible to users, namely /pipelines, /components, and /processes. For monitoring
purposes a /metrics endpoint is also available for the use of PrometheusIO.
These are only the base URLs but may be followed by more specific path elements to call different methods.
The full list of avaiable endpoints including a description and their possible response codes can be seen in
the Table X.

| Method | Endpoint                 | Response Codes | Accept |
|:-------|--------------------------|----------------|:------:|
| GET    | /pipelines/:id           | 404, 200       |        |
| GET    | /pipelines/              | 404, 200       |        |
| POST   | /pipelines               | 400, 200       |  json  |
| POST   | /pipelines/start/:id     |                |        |
| PUT    | /pipeline/stop/:id       |                |        |
| PUT    | /pipelines/:id           |                |  json  |
| DELETE | /pipelines/:id           |                |        |
| GET    | /components/:id          |                |        |
| GET    | /components              |                |        |
| POST   | /components              |                |        |
| PUT    | /components/:id          |                |        |
| DELETE | /components/:id          |                |        |
| GET    | /processes/:id           |                |        |
| GET    | /processes               |                |        |
| GET    | /processes/:id/events    |                |        |
| GET    | /processes/:id/documents |                |        |
| POST   | /processes               |                |  json  |
| PUT    | /processes/:id           |                |        |
| DELETE | /processes/:id           |                |        |
| GET    | /metrics                 |                |        |

### 4. Data

Processing only text through Java and especially through the web interface quickly becomes unproductive and
does not make use of DUUI's full potential. The question becomes, where does the (big) data for processing
come from and where while it go once finished. To provide a solution for fast and simple data flow the
IDUUIDocumentHandler interface has been implemented. It consists of 6 simple methods for interaction with
data:

```java

public interface IDUUIDocumentHandler {

    List<DUUIDocument> listDocuments(String path, String fileExtension, boolean recursive) throws IOException;

    default List<DUUIDocument> listDocuments(String path, String fileExtension) throws IOException {
        return listDocuments(path, fileExtension, false);
    }

    DUUIDocument readDocument(String path) throws IOException;

    List<DUUIDocument> readDocuments(List<String> paths) throws IOException;

    void writeDocument(DUUIDocument document, String path) throws IOException;

    void writeDocuments(List<DUUIDocument> documents, String path) throws IOException;

}
```

where a document is an instance of the DUUIDocument class holding content and metadata of the file to process.
DUUI provides implementations for read and write operations to and from Dropbox, Min.io (an Amazon s3
compatible object-storage) and a variant for working locally in the file system. To use these cloud solutions
the user must authorize DUUI to make requests in their name. For Dropbox this means the user has to go through
an OAuth2.0 authorization flow on the Dropbox website and grant DUUI access to create a folder in their
account that is used to perform IO operations. When the user accepts the proposed connection, Dropbox returns
a one time code, that is used to generate an access token. The token can be sent to Dropbox in requests as
means of authorization. While this works, it is not practical in the sense that access tokens are only usable
for a limited amount of time until they have to be generated again. To prevent the user from constantly
needing to go through the same authorization flow, another strategy is used. The code returned from the flow
can, in addition to the creation of the access token, also be used to generate a refresh token. A refresh
token is similar to an access token, but can be used by DUUI to generate a new access token as required. This
eliminates the need for a repeated authorization while at the same time leaving the possibility to revoke
access at any point in time. Because DUUI only gets access to a specific folder in the user's Dropbox account
and authorization can be managed by said user, the usage of Dropbox as a data source and dump is a quick and
easy way to get started.

Minio also requires the user to provide DUUI with an access and secret key for authorized requests. In this
case the user must create an account for DUUI in their s3 solution and allow DUUI to store both a username
(access key) and a password (secret key) for a specified endpoint. These credentials can be managed by the
user and define the scopes and buckets DUUI has access to. There is no need for repeated authorization flows
as the username and password are sufficient for making authorized requests on the user's behalf.

Due to the simple design of the DocumentHandler interface other cloud or database providers can easily be
integrated to extend the supported technologies and target audience. To name a few, OneDrive and Google Drive
make excellent cloud storage providers that are widely used and secure. The issue with the implementation of
DocumentHandlers using a database backend is that files are usually not stored in databases directly. Getting
relevant data from a database requires knowledge about the structure of the database. Identifying data in a
cloud storage is simply done through the absolute path of the file, but in a database data could be split into
multiple columns or fields. Querying is challenging when all information is passed in a single string. For
MongoDB this could be realized by defining the format of the provided path in a way to uniquely identify the
data to be queried and how to aggregate it.

### 5. Evaluation
