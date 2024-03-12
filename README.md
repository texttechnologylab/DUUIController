![SvelteKit]({https://img.shields.io/badge/SvelteKit-FF3E00?style=for-the-badge&logo=Svelte&logoColor=white})

![Svelte]({https://img.shields.io/badge/Svelte-4A4A55?style=for-the-badge&logo=svelte&logoColor=FF3E00})

# DUUIController

The DUUIController complements the NLP framework DUUI by providing a Web API and a graphical web-based user interface.

For running, create a config.properties file that contains values for the following variables:

- DBX_APP_KEY =
- DBX_APP_SECRET =
- DBX_REDIRECT_URL =
- PORT =
- HOST =
- FILE_UPLOAD_DIRECTORY =
- MONGO_HOST =
- MONGO_PORT =
- MONGO_DB =
- MONGO_USER =
- MONGO_PASSWORD =

> Variables with the DBX prefix can be omitted.

The path to the config file should be passed as a command line argument.

### To build follow these steps for now:

- Run `maven clean package -DskipTests`
- Set the correct working directory (where the jar is located)
- run `java -jar DUUIRestService.jar PATH/TO/config.properties`

### 1. Metrics

DUUI collects metrics at document level and stores these in `DUUIDocument` objects for later storage in a database. Metrics include:

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

### 2. Monitoring

Docker Unified UIMA Interface used simple logging to the console as a way to provide a way of monitoring the state on progress of a pipeline. To allow for more sophisticated logging of important stages and tracking the exact timestamp at which these events occur, an event system has been added. Events can be added at any desired stage of a pipeline by calling the composer’s addEvent method. The method then creates a DUUIEvent object that holds a timestamp, sender, and message. In the addEvent method, the event can be processed further, stored in a database, or simply logged to the console by setting the composer’s DebugLevel which acts as a filter. In the process of adding events in various places, the possibility to cancel a process has been added as well by providing the composer’s shutdown flag in multiple processing steps. Cancelling a process is then done by simply calling the shutdown method on the composer which signals drivers and components to stop as soon as possible. Through the addition of a `DUUIDocument` class, a per file monitoring is achieved. A `DUUIDocument` object not only contains file metadata and content but also tracks many metrics including progress, status, file size, durations for the different steps in the pipeline, and errors.

### 3. Backend

To reflect database CRUD operations, those methods have beend mapped to routes or endpoints that can be reached by users. To manipulate resources in the database the user is asked to provide an authorization method like an API key, that has to be generated in the web-interface. All endpoints communicate via REST and transfer data in the json format for consistency and ease of conversion for the use with MongoDB.

All available endpoints are located in the `Methods.java` file.

### 4. Data

The addition of the `IDUUIDocumentHandler` interface to DUUI provides easy integration with
a user’s cloud storage of choice. An implementation of the interface for a specific provider
includes five basic methods that are used to interact with the API of the service. These five
methods offer a way to read, write, and list files at a specific location with the option to do so recursively. The read and list methods return DUUIDocuments that are the container for
the files to be processed while also storing metrics during processing. There is also an im-
plementation called `DUUILocalDocumentHandler` for reading from and writing to disk. The
simple interface design allows for the implementation of practically any third party cloud
storage as a provider as long as an API to interact with the data exists. As a starting point, two cloud storage services namely, Dropbox and MinIO, have been implemented for DUUI.
Using these implementations requires the user to provide credentials for authorization.

### IDUUIDocumentHandler interface

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

### Example for using a DocumentHandler with DUUI

```java

DUUIComposer composer = new DUUIComposer()
.withLuaContext(new DUUILuaContext().withJsonLibrary())
.withSkipVerification(true)
// Prints all events with a DebugLevel >= DEBUG
.withDebugLevel(DUUIComposer.DebugLevel.DEBUG)
.withWorkers(5)
.withIgnoreErrors(true);

// Create a DUUIMinioDocumentHandler with an endpoint, username and password
DUUIMinioDocumentHandler minio = new DUUIMinioDocumentHandler(
    endpoint,
    username,
    password
);

DUUIDocumentReader reader = ``DUUIDocumentReader
    .builder(composer)
    .withInputHandler(minio)
    .withInputPath("input/sample_txt")
    .withInputFileExtension(".txt")
    .withOutputHandler(minio)
    .withOutputPath("output/xmi")
    .withOutputFileExtension(".xmi")
    .withRecursive(true) // Look for documents recursively
    .withSortBySize(true) // Sort files in ascending order
    .withCheckTarget(true) // Filter already processed documents
    .withAddMetadata(true)
    .withMinimumDocumentSize(1024 * 3) // Files must be at least 3 kB in size
    .build();

composer.addDriver(new DUUIUIMADriver());
composer.addDriver(new DUUIDockerDriver());

composer
    .add(new DUUIUIMADriver.Component(createEngineDescription(BreakIteratorSegmenter.class))
    .withName("Tokenizer"));
composer
    .add(new DUUIDockerDriver.Component("docker.texttechnologylab.org/gervader_duui:latest")
    .withName("GerVADER"));

composer.run(reader, "example-minio");
composer.shutdown();
```

When used in a process with DUUI, a handler is always used by a `DUUIDocumentReader`, a class that is responsible for the pre-processing of documents and managing both read and write operations for the installed handlers. When the composer’s run method for using a `DUUIDocumentReader` is called, file metadata is retrieved by calling the listDocuments method. This initial listing of documents in the source location is followed by multiple filters that may reduce the number of files to be processed, depending on the settings that were passed to the reader. The remaining documents are then stored and processed by one or multiple threads or workers.

The actual file content is stored as raw bytes in a `DUUIDocument` object during processing
and cleared when the document has been uploaded to the output location or is otherwise fin-
ished. Clearing the bytes after processing the document is done to reduce peaks in memory
usage that would occur if all files were stored in memory at once. The final part of writing
files is planned to be extracted into a separate class (`DUUIDocumentWriter`) in the future but has been implemented here for simplicity.

### 5. Evaluation

The web interface's perceived usability has been evaluated through a testing process in which different users completed a task and filled out a feedback survey.
