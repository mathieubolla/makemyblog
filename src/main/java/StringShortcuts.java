public class StringShortcuts {
    public static String makeTitleFrom(String baseName) {
        String withExtension = baseName.substring(baseName.lastIndexOf('/') + 1, baseName.length()).replace('_', ' ');
        if (withExtension.contains(".")) {
            return withExtension.substring(0, withExtension.lastIndexOf('.'));
        }
        return withExtension;
    }
}
