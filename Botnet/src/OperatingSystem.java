public enum OperatingSystem {
    WINDOWS("Windows"),
    LINUX("Linux"),
    MACOS("macOS"),
    SOLARIS("Solaris"),
    ANDROID("Android"),
    UNKNOWN("Unknown");

    private final String humanReadableName;

    private OperatingSystem(final String string3) {
        this.humanReadableName = string3;
    }

    public String getFancy() {
        return this.humanReadableName;
    }

    public String toString() {
        return this.humanReadableName;
    }

    public static boolean isLinux() {
        return get() != OperatingSystem.WINDOWS;
    }

    public static OperatingSystem get() {
        final String lowerCase = System.getProperty("os.name").toLowerCase();
        return lowerCase.contains("win") ? OperatingSystem.WINDOWS : (lowerCase.contains("mac") ? OperatingSystem.MACOS : (lowerCase.contains("solaris") ? OperatingSystem.SOLARIS : (lowerCase.contains("sunos") ? OperatingSystem.SOLARIS : (lowerCase.contains("linux") ? OperatingSystem.LINUX : (lowerCase.contains("unix") ? OperatingSystem.LINUX : OperatingSystem.UNKNOWN)))));
    }
}
