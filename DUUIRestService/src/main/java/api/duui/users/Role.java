package api.duui.users;

public enum Role {
    NONE,
    USER,
    ADMIN,
    TRIAL,
    SYSTEM;

    public static Role fromString(String role) {
        return switch (role.toLowerCase()) {
            case "user" -> USER;
            case "admin" -> ADMIN;
            case "system" -> SYSTEM;
            case "trial" -> TRIAL;
            default -> NONE;
        };
    }
}
