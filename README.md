# DUUIController

### 1. Metriken

`DUUI` stellt bereits einige Metriken für die Performance von Komponenten zur Verfügung (Wartezeit, Laufzeit
etc.). Diese vorhanden Metriken sollen erweitert werden.

Einige Ideen für weitere Metriken:

- Wie viele Kerne werden genutzt
- Wie viele Server stehen noch zur Verfügung / sind in Benutzung
- Wie weit ist die Verarbeitung der Dokumente
- Wie viel Speicher wird aktuell genutzt
- Wie viele Dokumente wurden erfolgreich verarbeitet
- Welche Fehler sind aufgetreten, wieso, wie oft

### 2. Visualisierung der Metriken / des Programmablaufs

`DUUI` ist aktuell eher eine "Blackbox" für den Benutzer, da zwar klar ist welche Pipelines ausgeführt werden,
aber nicht wie der Status einzelner Komponenten / Dokumente ist.

Die Visualisierung des Programmablaufs und die dazugehörigen Metriken soll einen Aufschluss über die
Performance und den Status der Applikation geben. Gleichzeitig kann über ein UserInterface (UI) auch eine
gewisse Steuerung für den Benutzer ermöglicht werden (siehe `3. Steuerprogramm`).

### 3. Steuerprogramm

| Method | Endpoint                 | Description                           | Parameters     | Accept |
|:------:|--------------------------|---------------------------------------|----------------|:------:|
|  GET   | /pipelines/:id           | Retrieve a pipeline by its id         | field          |        |
|  GET   | /pipelines/:id/processes | Retrieve all processes for a pipeline |
|  GET   | /pipelines               | Retrieve all pipelines                | limit & offset |        |
|  POST  | /pipelines               | Create a pipeline                     |                |  json  |
|  PUT   | /pipelines/:id           | Update a pipeline                     |                |  json  |
| DELETE | /pipelines/:id           | Delete a pipeline                     |
|  GET   | /processes/:id           | Retrieve all processes for a pipeline |
|  GET   | /processes/:id/status    | Retrieve the status of a process      |
|  GET   | /processes/:id/progress  | Retrieve the progress of a process    |
|  POST  | /processes               | Create a process                      |                |  json  |
|  PUT   | /processes/:id           | Cancel a process                      |
|  GET   | /metrics                 | Retrieve metrics for the api          |

### 4. Daten

Der Data-Loader (z.B. S3) soll die Bereitstellung von Ressourcen schnell und effizient für verschieden Quellen
ermöglichen.

### 5. Evaluation
