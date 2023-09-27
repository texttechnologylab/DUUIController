from dataclasses import dataclass


@dataclass
class DUUIUser:

    user_id: str
    email: str
    session: str

