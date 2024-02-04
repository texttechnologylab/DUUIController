from enum import StrEnum


class DUUIStatus(StrEnum):
    ACTIVE = "Active"
    ANY = "Any"
    CANCELLED = "Cancelled"
    COMPLETED = "Completed"
    DECODE = "Decode"
    DESERIALIZE = "Deserialize"
    DOWNLOAD = "Download"
    FAILED = "Failed"
    IDLE = "Idle"
    INACTIVE = "Inactive"
    INPUT = "Input"
    INSTANTIATING = "Instatiating"
    OUTPUT = "Output"
    SETUP = "Setup"
    SHUTDOWN = "Shutdown"
    SKIPPED = "Skipped"
    IMAGESTART = "Starting"
    UNKNOWN = "Unknow"
    WAITING = "Waiting"


class Driver(StrEnum):
    UIMA = "DUUIUIMADriver"
    REMOTE = "DUUIRemoteDriver"
    DOCKER = "DUUIDockerDriver"
    SWARM = "DUUISwarmDriver"
    KUBERNETES = "DUUIKubernetesDriver"
