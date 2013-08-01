public class StringShortcuts {
    public static String makeTitleFrom(String baseName) {
        return baseName.substring(baseName.lastIndexOf('/') + 1, baseName.length()).replace('_', ' ');
    }

}
